package com.example.gsb.json;

/** JSON {@code null}. */
public final class JsonNull extends JsonValue {

    public static final JsonNull INSTANCE = new JsonNull();

    private JsonNull() {
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    String typeName() {
        return "null";
    }

    @Override
    public String toJson() {
        return "null";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof JsonNull;
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
