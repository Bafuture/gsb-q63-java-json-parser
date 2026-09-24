package com.example.gsb.json;

/**
 * JSON 词法/语法错误。message 中已包含 "line X, column Y" 前缀，
 * 同时暴露 line/column 字段便于程序化处理。
 */
public class JsonException extends RuntimeException {

    private final int line;
    private final int column;

    public JsonException(int line, int column, String message) {
        super("line " + line + ", column " + column + ": " + message);
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
