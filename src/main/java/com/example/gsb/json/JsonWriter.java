package com.example.gsb.json;

/** Internal serialization helpers shared by the value nodes. */
final class JsonWriter {

    private JsonWriter() {
    }

    static void writeObject(StringBuilder sb, JsonObject object) {
        sb.append('{');
        boolean first = true;
        for (var entry : object.members().entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append(writeString(entry.getKey())).append(':').append(entry.getValue().toJson());
        }
        sb.append('}');
    }

    static void writeArray(StringBuilder sb, JsonArray array) {
        sb.append('[');
        boolean first = true;
        for (JsonValue element : array.elements()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append(element.toJson());
        }
        sb.append(']');
    }

    /** Writes a JSON string literal, escaping all characters that require it. */
    static String writeString(String value) {
        StringBuilder sb = new StringBuilder(value.length() + 2);
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
