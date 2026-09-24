package com.example.gsb.json;

/**
 * JSON 布尔值，仅两个实例。
 */
public final class JsonBoolean implements JsonValue {

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
    public JsonBoolean asBoolean() {
        return this;
    }

    @Override
    public String toJson() {
        return value ? "true" : "false";
    }

    @Override
    public String toPrettyJson() {
        return toJson();
    }

    @Override
    public String toString() {
        return toJson();
    }
}
