package com.example.gsb.json;

/**
 * Common base class of the immutable JSON value tree.
 *
 * <p>The concrete node types are:
 * {@link JsonObject}, {@link JsonArray}, {@link JsonString},
 * {@link JsonNumber}, {@link JsonBoolean} and {@link JsonNull}.
 */
public abstract class JsonValue {

    /**
     * Serializes this value to compact JSON text. The output always conforms to
     * RFC 8259, so it can be fed back into {@link JsonParser#parse(String)}.
     */
    public abstract String toJson();

    public JsonObject asObject() {
        throw new ClassCastException("Not a JSON object: " + typeName());
    }

    public JsonArray asArray() {
        throw new ClassCastException("Not a JSON array: " + typeName());
    }

    public String asString() {
        throw new ClassCastException("Not a JSON string: " + typeName());
    }

    public java.math.BigDecimal asNumber() {
        throw new ClassCastException("Not a JSON number: " + typeName());
    }

    public boolean asBoolean() {
        throw new ClassCastException("Not a JSON boolean: " + typeName());
    }

    public boolean isNull() {
        return false;
    }

    abstract String typeName();

    @Override
    public abstract boolean equals(Object other);

    @Override
    public abstract int hashCode();

    @Override
    public String toString() {
        return toJson();
    }
}
