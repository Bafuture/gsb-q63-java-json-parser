package com.example.gsb.json;

import java.util.Objects;

/** JSON string node. */
public final class JsonString extends JsonValue {

    private final String value;

    public JsonString(String value) {
        this.value = Objects.requireNonNull(value, "string value must not be null");
    }

    public String value() {
        return value;
    }

    @Override
    public String asString() {
        return value;
    }

    @Override
    String typeName() {
        return "string";
    }

    @Override
    public String toJson() {
        return JsonWriter.writeString(value);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof JsonString s && s.value.equals(value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
