package com.example.gsb.json;

import java.util.Objects;

/**
 * JSON 字符串。
 */
public final class JsonString implements JsonValue {

    private final String value;

    public JsonString(String value) {
        this.value = Objects.requireNonNull(value, "字符串值不能为 null，请使用 JsonNull.INSTANCE");
    }

    public String value() {
        return value;
    }

    @Override
    public JsonString asString() {
        return this;
    }

    @Override
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        JsonWriter.write(this, sb, -1, 0);
        return sb.toString();
    }

    @Override
    public String toPrettyJson() {
        return toJson();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof JsonString other && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return toJson();
    }
}
