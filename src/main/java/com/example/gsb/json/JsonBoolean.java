package com.example.gsb.json;

/** JSON 布尔值，单例。 */
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

    public boolean getValue() {
        return value;
    }

    @Override
    public boolean asBoolean() {
        return value;
    }

    @Override
    void writeJson(StringBuilder sb) {
        sb.append(value ? "true" : "false");
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof JsonBoolean other && value == other.value;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }
}
