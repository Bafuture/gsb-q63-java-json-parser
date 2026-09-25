package com.example.gsb.json;

/**
 * Thrown when the input is not valid JSON. The error position is 1-based and
 * points at the first character that could not be accepted by the parser.
 */
public class JsonParseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int line;
    private final int column;

    public JsonParseException(String message, int line, int column) {
        super(message + " (line " + line + ", column " + column + ")");
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
