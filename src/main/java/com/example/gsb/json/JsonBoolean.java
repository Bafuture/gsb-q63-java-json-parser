package com.example.gsb.json;

/** JSON boolean literal. */
public final class JsonBoolean extends JsonValue {

    public static final JsonBoolean TRUE = new JsonBoolean(true);
    public static final JsonBoolean FALSE = new JsonBoolean(false);

    private final boolean value;

    private JsonBoolean(boolean value) {
        this.value = value;
    }

    public static JsonBoolean of(boolean value) {
        return value ? TRUE : FALSE;
    }

    public boolean value() {
        return value;
    }

    @Override
    public boolean asBoolean() {
        return value;
    }

    @Override
    String typeName() {
        return "boolean";
    }

    @Override
    public String toJson() {
        return value ? "true" : "false";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof JsonBoolean b && b.value == value;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }
}
