package com.example.gsb.json;

/** A lexical token with its 1-based position in the source text. */
final class Token {

    enum Type {
        LEFT_BRACE, RIGHT_BRACE, LEFT_BRACKET, RIGHT_BRACKET,
        COMMA, COLON, STRING, NUMBER, TRUE, FALSE, NULL, EOF
    }

    final Type type;
    /** Decoded string value for STRING, raw lexeme for NUMBER, null otherwise. */
    final String text;
    final int line;
    final int column;

    Token(Type type, String text, int line, int column) {
        this.type = type;
        this.text = text;
        this.line = line;
        this.column = column;
    }

    static Token simple(Type type, int line, int column) {
        return new Token(type, null, line, column);
    }
}
