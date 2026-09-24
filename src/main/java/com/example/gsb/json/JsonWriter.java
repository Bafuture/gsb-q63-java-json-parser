package com.example.gsb.json;

/**
 * 序列化器：把 {@link JsonValue} 对象树输出为合法 JSON 文本。
 * indent &lt; 0 表示紧凑输出，否则为每层缩进的空格数（美化输出）。
 */
final class JsonWriter {

    private JsonWriter() {
    }

    static void write(JsonValue value, StringBuilder out, int indent, int level) {
        if (value instanceof JsonObject obj) {
            writeObject(obj, out, indent, level);
        } else if (value instanceof JsonArray arr) {
            writeArray(arr, out, indent, level);
        } else if (value instanceof JsonString str) {
            writeEscaped(str.value(), out);
        } else {
            out.append(value.toJson());
        }
    }

    private static void writeObject(JsonObject obj, StringBuilder out, int indent, int level) {
        if (obj.size() == 0) {
            out.append("{}");
            return;
        }
        out.append('{');
        boolean first = true;
        for (var entry : obj.members().entrySet()) {
            if (!first) {
                out.append(',');
            }
            first = false;
            newlineAndIndent(out, indent, level + 1);
            writeEscaped(entry.getKey(), out);
            out.append(':');
            if (indent >= 0) {
                out.append(' ');
            }
            write(entry.getValue(), out, indent, level + 1);
        }
        newlineAndIndent(out, indent, level);
        out.append('}');
    }

    private static void writeArray(JsonArray arr, StringBuilder out, int indent, int level) {
        if (arr.size() == 0) {
            out.append("[]");
            return;
        }
        out.append('[');
        boolean first = true;
        for (JsonValue element : arr.elements()) {
            if (!first) {
                out.append(',');
            }
            first = false;
            newlineAndIndent(out, indent, level + 1);
            write(element, out, indent, level + 1);
        }
        newlineAndIndent(out, indent, level);
        out.append(']');
    }

    private static void newlineAndIndent(StringBuilder out, int indent, int level) {
        if (indent < 0) {
            return;
        }
        out.append('\n');
        out.append(" ".repeat(indent * level));
    }

    /**
     * 输出带引号并转义的字符串。转义规则：
     * 双引号、反斜杠、以及 U+0000–U+001F 的控制字符必须转义；
     * 其中 \b \t \n \f \r 使用短形式，其余控制字符使用 \\u00XX 形式。
     * 非 ASCII 字符原样输出（JSON 文本默认 UTF-8 编码）。
     */
    static void writeEscaped(String s, StringBuilder out) {
        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\b' -> out.append("\\b");
                case '\t' -> out.append("\\t");
                case '\n' -> out.append("\\n");
                case '\f' -> out.append("\\f");
                case '\r' -> out.append("\\r");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }
}
