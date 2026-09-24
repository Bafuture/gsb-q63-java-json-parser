package com.example.gsb.json;

/**
 * JSON 语法错误异常。携带 1 起始的行号与列号，并给出可读的中文提示。
 */
public class JsonParseException extends RuntimeException {

    private final int line;
    private final int column;

    public JsonParseException(int line, int column, String detail) {
        super("第 " + line + " 行第 " + column + " 列：" + detail);
        this.line = line;
        this.column = column;
    }

    /** 出错位置所在的行号（从 1 开始）。 */
    public int getLine() {
        return line;
    }

    /** 出错位置所在的列号（从 1 开始）。 */
    public int getColumn() {
        return column;
    }
}
