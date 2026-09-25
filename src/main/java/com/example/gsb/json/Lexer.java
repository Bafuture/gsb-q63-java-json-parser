package com.example.gsb.json;

/**
 * Hand-written lexer that turns the input text into a stream of {@link Token}s.
 * Line and column numbers are 1-based; a newline resets the column counter.
 */
final class Lexer {

    private final String input;
    private int pos;
    private int line = 1;
    private int column = 1;

    Lexer(String input) {
        this.input = input;
    }

    Token next() {
        skipWhitespace();
        if (pos >= input.length()) {
            return Token.simple(Token.Type.EOF, line, column);
        }
        int startLine = line;
        int startColumn = column;
        char c = input.charAt(pos);
        switch (c) {
            case '{' -> { advance(); return Token.simple(Token.Type.LEFT_BRACE, startLine, startColumn); }
            case '}' -> { advance(); return Token.simple(Token.Type.RIGHT_BRACE, startLine, startColumn); }
            case '[' -> { advance(); return Token.simple(Token.Type.LEFT_BRACKET, startLine, startColumn); }
            case ']' -> { advance(); return Token.simple(Token.Type.RIGHT_BRACKET, startLine, startColumn); }
            case ',' -> { advance(); return Token.simple(Token.Type.COMMA, startLine, startColumn); }
            case ':' -> { advance(); return Token.simple(Token.Type.COLON, startLine, startColumn); }
            case '"' -> { return new Token(Token.Type.STRING, readString(), startLine, startColumn); }
            case 't' -> { readKeyword("true"); return Token.simple(Token.Type.TRUE, startLine, startColumn); }
            case 'f' -> { readKeyword("false"); return Token.simple(Token.Type.FALSE, startLine, startColumn); }
            case 'n' -> { readKeyword("null"); return Token.simple(Token.Type.NULL, startLine, startColumn); }
            default -> {
                if (c == '-' || (c >= '0' && c <= '9')) {
                    return new Token(Token.Type.NUMBER, readNumber(), startLine, startColumn);
                }
                throw error("Unexpected character '" + printable(c) + "'", startLine, startColumn);
            }
        }
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

    private void readKeyword(String keyword) {
        int startLine = line;
        int startColumn = column;
        for (int i = 0; i < keyword.length(); i++) {
            if (pos >= input.length() || input.charAt(pos) != keyword.charAt(i)) {
                throw error("Invalid literal, expected '" + keyword + "'", startLine, startColumn);
            }
            advance();
        }
    }

    private String readString() {
        StringBuilder sb = new StringBuilder();
        advance(); // opening quote
        while (true) {
            if (pos >= input.length()) {
                throw error("Unterminated string literal", line, column);
            }
            char c = input.charAt(pos);
            if (c == '"') {
                advance();
                return sb.toString();
            }
            if (c == '\\') {
                advance();
                sb.append(readEscape());
            } else if (c < 0x20) {
                throw error("Unescaped control character (U+" + String.format("%04X", (int) c)
                        + ") in string, use an escape sequence", line, column);
            } else {
                sb.append(c);
                advance();
            }
        }
    }

    private String readEscape() {
        if (pos >= input.length()) {
            throw error("Unterminated escape sequence", line, column);
        }
        char esc = input.charAt(pos);
        switch (esc) {
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
            default -> throw error("Invalid escape sequence '\\" + printable(esc) + "'", line, column);
        }
    }

    private String readUnicodeEscape() {
        int codeUnit = readHexQuad();
        if (Character.isHighSurrogate((char) codeUnit)) {
            // A high surrogate must be followed by a \uDC00-\uDFFF escape.
            int pairLine = line;
            int pairColumn = column;
            if (pos + 1 < input.length() && input.charAt(pos) == '\\' && input.charAt(pos + 1) == 'u') {
                advance();
                advance();
                int low = readHexQuad();
                if (Character.isLowSurrogate((char) low)) {
                    return new String(Character.toChars(
                            Character.toCodePoint((char) codeUnit, (char) low)));
                }
                throw error("Invalid low surrogate U+" + String.format("%04X", low)
                        + " after high surrogate", pairLine, pairColumn);
            }
            throw error("Unpaired high surrogate U+" + String.format("%04X", codeUnit)
                    + ", expected a \\uDC00-\\uDFFF escape", pairLine, pairColumn);
        }
        if (Character.isLowSurrogate((char) codeUnit)) {
            throw error("Unpaired low surrogate U+" + String.format("%04X", codeUnit), line, column);
        }
        return Character.toString((char) codeUnit);
    }

    private int readHexQuad() {
        int startLine = line;
        int startColumn = column;
        if (pos + 4 > input.length()) {
            throw error("Incomplete \\u escape, expected 4 hex digits", startLine, startColumn);
        }
        int codePoint = 0;
        for (int i = 0; i < 4; i++) {
            char h = input.charAt(pos);
            int digit = Character.digit(h, 16);
            if (digit < 0) {
                throw error("Invalid hex digit '" + printable(h) + "' in \\u escape", line, column);
            }
            codePoint = (codePoint << 4) | digit;
            advance();
        }
        return codePoint;
    }

    private String readNumber() {
        int start = pos;
        int startLine = line;
        int startColumn = column;
        if (peek() == '-') {
            advance();
        }
        // Integer part: 0 or [1-9][0-9]*
        if (peek() == '0') {
            advance();
            if (isDigit(peek())) {
                throw error("Leading zeros are not allowed in numbers", line, column);
            }
        } else if (isDigitOneToNine(peek())) {
            while (isDigit(peek())) {
                advance();
            }
        } else {
            throw error("Invalid number, expected a digit", startLine, startColumn);
        }
        // Fraction part
        if (peek() == '.') {
            advance();
            if (!isDigit(peek())) {
                throw error("Invalid number, expected a digit after the decimal point", line, column);
            }
            while (isDigit(peek())) {
                advance();
            }
        }
        // Exponent part
        if (peek() == 'e' || peek() == 'E') {
            advance();
            if (peek() == '+' || peek() == '-') {
                advance();
            }
            if (!isDigit(peek())) {
                throw error("Invalid number, expected a digit in the exponent", line, column);
            }
            while (isDigit(peek())) {
                advance();
            }
        }
        return input.substring(start, pos);
    }

    private char peek() {
        return pos < input.length() ? input.charAt(pos) : '\0';
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

    private JsonParseException error(String message, int errorLine, int errorColumn) {
        return new JsonParseException(message, errorLine, errorColumn);
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isDigitOneToNine(char c) {
        return c >= '1' && c <= '9';
    }

    private static String printable(char c) {
        if (c < 0x20 || c == 0x7F) {
            return "U+" + String.format("%04X", (int) c);
        }
        return Character.toString(c);
    }
}
