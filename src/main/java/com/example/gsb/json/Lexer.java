package com.example.gsb.json;

import java.math.BigDecimal;

/**
 * 词法分析器：把输入字符流切分为 Token 序列。
 * 每个 Token 都记录其起始行号与列号（均从 1 开始），供错误定位使用。
 */
final class Lexer {

    enum TokenType {
        LBRACE, RBRACE, LBRACKET, RBRACKET, COMMA, COLON,
        STRING, NUMBER, TRUE, FALSE, NULL, EOF
    }

    record Token(TokenType type, String text, BigDecimal numberValue, int line, int column) {

        static Token simple(TokenType type, int line, int column) {
            return new Token(type, null, null, line, column);
        }

        static Token string(String value, int line, int column) {
            return new Token(TokenType.STRING, value, null, line, column);
        }

        static Token number(String text, int line, int column) {
            return new Token(TokenType.NUMBER, text, new BigDecimal(text), line, column);
        }

        /** 用于错误消息中的可读描述。 */
        String describe() {
            return switch (type) {
                case LBRACE -> "'{'";
                case RBRACE -> "'}'";
                case LBRACKET -> "'['";
                case RBRACKET -> "']'";
                case COMMA -> "','";
                case COLON -> "':'";
                case STRING -> "字符串 \"" + abbreviate(text) + "\"";
                case NUMBER -> "数字 " + text;
                case TRUE -> "'true'";
                case FALSE -> "'false'";
                case NULL -> "'null'";
                case EOF -> "输入结束（EOF）";
            };
        }

        private static String abbreviate(String s) {
            return s.length() <= 20 ? s : s.substring(0, 20) + "...";
        }
    }

    private final String src;
    private final int length;
    private int pos = 0;
    private int line = 1;
    private int column = 1;

    Lexer(String src) {
        this.src = src;
        this.length = src.length();
    }

    /** 读取下一个 Token。 */
    Token next() {
        skipWhitespace();
        int tokenLine = line;
        int tokenColumn = column;
        if (pos >= length) {
            return Token.simple(TokenType.EOF, tokenLine, tokenColumn);
        }
        char c = src.charAt(pos);
        switch (c) {
            case '{' -> { advance(); return Token.simple(TokenType.LBRACE, tokenLine, tokenColumn); }
            case '}' -> { advance(); return Token.simple(TokenType.RBRACE, tokenLine, tokenColumn); }
            case '[' -> { advance(); return Token.simple(TokenType.LBRACKET, tokenLine, tokenColumn); }
            case ']' -> { advance(); return Token.simple(TokenType.RBRACKET, tokenLine, tokenColumn); }
            case ',' -> { advance(); return Token.simple(TokenType.COMMA, tokenLine, tokenColumn); }
            case ':' -> { advance(); return Token.simple(TokenType.COLON, tokenLine, tokenColumn); }
            case '"' -> { return Token.string(readString(), tokenLine, tokenColumn); }
            case 't' -> { readLiteral("true", tokenLine, tokenColumn); return Token.simple(TokenType.TRUE, tokenLine, tokenColumn); }
            case 'f' -> { readLiteral("false", tokenLine, tokenColumn); return Token.simple(TokenType.FALSE, tokenLine, tokenColumn); }
            case 'n' -> { readLiteral("null", tokenLine, tokenColumn); return Token.simple(TokenType.NULL, tokenLine, tokenColumn); }
            default -> {
                if (c == '-' || (c >= '0' && c <= '9')) {
                    return Token.number(readNumber(), tokenLine, tokenColumn);
                }
                if (isIdentifierStart(c)) {
                    // 形如 tru / nul 之类的残缺字面量，给出更友好的提示
                    throw error("无法识别的字面量，是否想写 'true'、'false' 或 'null'？");
                }
                throw error("遇到非法字符 '" + printable(c) + "'");
            }
        }
    }

    private void skipWhitespace() {
        while (pos < length) {
            char c = src.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                advance();
            } else {
                return;
            }
        }
    }

    private void readLiteral(String literal, int tokenLine, int tokenColumn) {
        for (int i = 0; i < literal.length(); i++) {
            if (pos >= length || src.charAt(pos) != literal.charAt(i)) {
                throw new JsonParseException(tokenLine, tokenColumn,
                        "无法识别的字面量，是否想写 '" + literal + "'？");
            }
            advance();
        }
        // 字面量后紧跟字母/数字也属于非法，如 truex
        if (pos < length && isIdentifierStart(src.charAt(pos))) {
            throw new JsonParseException(tokenLine, tokenColumn,
                    "字面量 '" + literal + "' 后不能紧跟字符 '" + src.charAt(pos) + "'");
        }
    }

    /**
     * 读取字符串（当前字符为开头的双引号），处理全部转义。
     */
    private String readString() {
        advance(); // 跳过开头的 "
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (pos >= length) {
                throw error("字符串未结束：缺少结尾的双引号 '\"'");
            }
            char c = src.charAt(pos);
            if (c == '"') {
                advance();
                return sb.toString();
            }
            if (c == '\\') {
                advance();
                sb.append(readEscape());
                continue;
            }
            if (c < 0x20) {
                throw error("字符串中不允许出现未转义的控制字符（U+"
                        + String.format("%04X", (int) c) + "），请使用转义序列");
            }
            sb.append(c);
            advance();
        }
    }

    private String readEscape() {
        if (pos >= length) {
            throw error("转义序列未结束：'\\' 后缺少内容");
        }
        char e = src.charAt(pos);
        switch (e) {
            case '"' -> { advance(); return "\""; }
            case '\\' -> { advance(); return "\\"; }
            case '/' -> { advance(); return "/"; }
            case 'b' -> { advance(); return "\b"; }
            case 'f' -> { advance(); return "\f"; }
            case 'n' -> { advance(); return "\n"; }
            case 'r' -> { advance(); return "\r"; }
            case 't' -> { advance(); return "\t"; }
            case 'u' -> {
                advance();
                return readUnicodeEscape();
            }
            default -> throw error("非法转义序列 '\\" + printable(e)
                    + "'，仅支持 \\\" \\\\ \\/ \\b \\f \\n \\r \\t \\uXXXX");
        }
    }

    /**
     * 读取 \\uXXXX（当前位置在 'u' 之后）。若为高代理项，则要求紧跟
     * 一个 \\uXXXX 低代理项并组合成完整字符。
     */
    private String readUnicodeEscape() {
        int first = readHex4();
        if (Character.isHighSurrogate((char) first)) {
            // 必须紧跟 \\uXXXX 低代理项
            if (pos + 1 < length && src.charAt(pos) == '\\' && src.charAt(pos + 1) == 'u') {
                advance();
                advance();
                int second = readHex4();
                if (!Character.isLowSurrogate((char) second)) {
                    throw error("代理项配对无效：高代理项后应跟低代理项（U+DC00–U+DFFF），"
                            + "实际为 U+" + String.format("%04X", second));
                }
                return new String(new char[]{(char) first, (char) second});
            }
            throw error("代理项配对无效：高代理项 U+" + String.format("%04X", first)
                    + " 后缺少 \\uXXXX 低代理项");
        }
        if (Character.isLowSurrogate((char) first)) {
            throw error("代理项配对无效：低代理项 U+" + String.format("%04X", first)
                    + " 不能单独出现");
        }
        return String.valueOf((char) first);
    }

    private int readHex4() {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            if (pos >= length) {
                throw error("\\u 转义不完整：需要 4 位十六进制数字");
            }
            char c = src.charAt(pos);
            int digit = Character.digit(c, 16);
            if (digit < 0) {
                throw error("\\u 转义包含非法字符 '" + printable(c) + "'，需要 4 位十六进制数字");
            }
            value = (value << 4) | digit;
            advance();
        }
        return value;
    }

    /**
     * 按 JSON 数字文法严格扫描：-?(0|[1-9]\d*)(\.\d+)?([eE][+-]?\d+)?
     * 返回原始文本，由调用方构造 BigDecimal（不丢精度）。
     */
    private String readNumber() {
        int start = pos;
        if (peek() == '-') {
            advance();
        }
        // 整数部分
        if (pos >= length) {
            throw error("数字不完整：'-' 后缺少数字");
        }
        char c = src.charAt(pos);
        if (c == '0') {
            advance();
            if (pos < length && Character.isDigit(src.charAt(pos))) {
                throw error("非法数字：整数部分以 0 开头（如 01），JSON 不允许前导零");
            }
        } else if (c >= '1' && c <= '9') {
            while (pos < length && Character.isDigit(src.charAt(pos))) {
                advance();
            }
        } else {
            throw error("非法数字：此处应为 0-9 的数字");
        }
        // 小数部分
        if (pos < length && src.charAt(pos) == '.') {
            advance();
            if (pos >= length || !Character.isDigit(src.charAt(pos))) {
                throw error("非法数字：小数点后至少应有 1 位数字");
            }
            while (pos < length && Character.isDigit(src.charAt(pos))) {
                advance();
            }
        }
        // 指数部分
        if (pos < length && (src.charAt(pos) == 'e' || src.charAt(pos) == 'E')) {
            advance();
            if (pos < length && (src.charAt(pos) == '+' || src.charAt(pos) == '-')) {
                advance();
            }
            if (pos >= length || !Character.isDigit(src.charAt(pos))) {
                throw error("非法数字：指数部分至少应有 1 位数字");
            }
            while (pos < length && Character.isDigit(src.charAt(pos))) {
                advance();
            }
        }
        return src.substring(start, pos);
    }

    private char peek() {
        return pos < length ? src.charAt(pos) : '\0';
    }

    private void advance() {
        if (pos < length) {
            if (src.charAt(pos) == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
            pos++;
        }
    }

    private JsonParseException error(String detail) {
        return new JsonParseException(line, column, detail);
    }

    private static boolean isIdentifierStart(char c) {
        return Character.isLetter(c);
    }

    private static String printable(char c) {
        if (c < 0x20 || c == 0x7F) {
            return "U+" + String.format("%04X", (int) c);
        }
        return String.valueOf(c);
    }
}
