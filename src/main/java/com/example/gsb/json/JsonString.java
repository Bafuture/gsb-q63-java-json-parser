package com.example.gsb.json;

import java.util.Objects;

/** JSON 字符串。 */
public final class JsonString extends JsonValue {

    private final String value;

    public JsonString(String value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public String getValue() {
        return value;
    }

    @Override
    public String asString() {
        return value;
    }

    @Override
    void writeJson(StringBuilder sb) {
        JsonWriter.writeString(value, sb);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof JsonString other && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
