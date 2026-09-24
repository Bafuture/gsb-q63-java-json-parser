package com.example.gsb.json;

import java.util.ArrayList;
import java.util.List;

/**
 * 词法分析器：把输入字符流切成 token 序列，每个 token 记录起始行列号。
 * 字符串与数字的语义处理（转义解码、精度保持）也在此完成。
 */
final class JsonLexer {

    enum TokenType {
        LBRACE, RBRACE, LBRACKET, RBRACKET, COMMA, COLON,
        STRING, NUMBER, TRUE, FALSE, NULL, EOF
    }

    record Token(TokenType type, String text, int line, int column) {
        @Override
        public String toString() {
            return switch (type) {
                case STRING -> "string \"" + text + "\"";
                case NUMBER -> "number " + text;
                case EOF -> "end of input";
                default -> "'" + text + "'";
            };
        }
    }

    private final String input;
    private int pos;
    private int line = 1;
    private int column = 1;

    JsonLexer(String input) {
        this.input = input;
    }

    List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (true) {
            skipWhitespace();
            int startLine = line;
            int startColumn = column;
            if (pos >= input.length()) {
                tokens.add(new Token(TokenType.EOF, "", startLine, startColumn));
                return tokens;
            }
            char c = input.charAt(pos);
            switch (c) {
                case '{' -> { advance(); tokens.add(simple(TokenType.LBRACE, "{", startLine, startColumn)); }
                case '}' -> { advance(); tokens.add(simple(TokenType.RBRACE, "}", startLine, startColumn)); }
                case '[' -> { advance(); tokens.add(simple(TokenType.LBRACKET, "[", startLine, startColumn)); }
                case ']' -> { advance(); tokens.add(simple(TokenType.RBRACKET, "]", startLine, startColumn)); }
                case ',' -> { advance(); tokens.add(simple(TokenType.COMMA, ",", startLine, startColumn)); }
                case ':' -> { advance(); tokens.add(simple(TokenType.COLON, ":", startLine, startColumn)); }
                case '"' -> tokens.add(readString(startLine, startColumn));
                case 't' -> { readLiteral("true", startLine, startColumn); tokens.add(simple(TokenType.TRUE, "true", startLine, startColumn)); }
                case 'f' -> { readLiteral("false", startLine, startColumn); tokens.add(simple(TokenType.FALSE, "false", startLine, startColumn)); }
                case 'n' -> { readLiteral("null", startLine, startColumn); tokens.add(simple(TokenType.NULL, "null", startLine, startColumn)); }
                default -> {
                    if (c == '-' || isDigit(c)) {
                        tokens.add(readNumber(startLine, startColumn));
                    } else {
                        throw error("unexpected character '" + printable(c) + "'");
                    }
                }
            }
        }
    }

    private Token simple(TokenType type, String text, int startLine, int startColumn) {
        return new Token(type, text, startLine, startColumn);
    }

    private void readLiteral(String literal, int startLine, int startColumn) {
        for (int i = 0; i < literal.length(); i++) {
            if (pos >= input.length() || input.charAt(pos) != literal.charAt(i)) {
                throw new JsonException(startLine, startColumn,
                        "invalid literal, expected '" + literal + "'");
            }
            advance();
        }
    }

    private Token readString(int startLine, int startColumn) {
        advance(); // 跳过开头的引号
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (pos >= input.length()) {
                throw error("unterminated string");
            }
            char c = input.charAt(pos);
            if (c == '"') {
                advance();
                return new Token(TokenType.STRING, sb.toString(), startLine, startColumn);
            }
            if (c == '\\') {
                advance();
                sb.append(readEscape());
            } else if (c < 0x20) {
                throw error("unescaped control character in string, use \\u"
                        + String.format("%04x", (int) c) + " instead");
            } else {
                sb.append(c);
                advance();
            }
        }
    }

    private char readEscape() {
        if (pos >= input.length()) {
            throw error("unterminated escape sequence");
        }
        char e = input.charAt(pos);
        switch (e) {
            case '"' -> { advance(); return '"'; }
            case '\\' -> { advance(); return '\\'; }
            case '/' -> { advance(); return '/'; }
            case 'b' -> { advance(); return '\b'; }
            case 'f' -> { advance(); return '\f'; }
            case 'n' -> { advance(); return '\n'; }
            case 'r' -> { advance(); return '\r'; }
            case 't' -> { advance(); return '\t'; }
            case 'u' -> {
                advance();
                return readUnicodeEscape();
            }
            default -> throw error("invalid escape sequence '\\" + printable(e) + "'");
        }
    }

    private char readUnicodeEscape() {
        if (pos + 4 > input.length()) {
            throw error("incomplete \\u escape, expected 4 hex digits");
        }
        int code = 0;
        for (int i = 0; i < 4; i++) {
            char h = input.charAt(pos);
            int digit = Character.digit(h, 16);
            if (digit < 0) {
                throw error("invalid hex digit '" + printable(h) + "' in \\u escape");
            }
            code = (code << 4) | digit;
            advance();
        }
        return (char) code;
    }

    /**
     * 按 JSON 数字文法读取词素：-?(0|[1-9]\d*)(\.\d+)?([eE][+-]?\d+)?
     * 这里只截取文本，精度由 JsonParser 用 BigDecimal 保证。
     */
    private Token readNumber(int startLine, int startColumn) {
        int start = pos;
        if (peek() == '-') {
            advance();
        }
        if (pos >= input.length()) {
            throw error("unexpected end of input in number");
        }
        if (peek() == '0') {
            advance();
            if (pos < input.length() && isDigit(peek())) {
                throw error("leading zeros are not allowed in numbers");
            }
        } else if (isDigit19(peek())) {
            while (pos < input.length() && isDigit(peek())) {
                advance();
            }
        } else {
            throw error("invalid number, expected a digit");
        }
        if (pos < input.length() && peek() == '.') {
            advance();
            if (pos >= input.length() || !isDigit(peek())) {
                throw error("invalid number, expected a digit after '.'");
            }
            while (pos < input.length() && isDigit(peek())) {
                advance();
            }
        }
        if (pos < input.length() && (peek() == 'e' || peek() == 'E')) {
            advance();
            if (pos < input.length() && (peek() == '+' || peek() == '-')) {
                advance();
            }
            if (pos >= input.length() || !isDigit(peek())) {
                throw error("invalid number, expected a digit in exponent");
            }
            while (pos < input.length() && isDigit(peek())) {
                advance();
            }
        }
        return new Token(TokenType.NUMBER, input.substring(start, pos), startLine, startColumn);
    }

    private void skipWhitespace() {
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                advance();
            } else {
                return;
            }
        }
    }

    private char peek() {
        return input.charAt(pos);
    }

    private void advance() {
        if (input.charAt(pos) == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        pos++;
    }

    private JsonException error(String message) {
        return new JsonException(line, column, message);
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isDigit19(char c) {
        return c >= '1' && c <= '9';
    }

    private static String printable(char c) {
        return c < 0x20 || c == 0x7F
                ? String.format("\\u%04x", (int) c)
                : Character.toString(c);
    }
}
