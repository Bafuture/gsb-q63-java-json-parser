package com.example.gsb.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** JSON array node. */
public final class JsonArray extends JsonValue {

    private final List<JsonValue> elements = new ArrayList<>();

    public int size() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public JsonValue get(int index) {
        return elements.get(index);
    }

    public JsonArray add(JsonValue value) {
        elements.add(Objects.requireNonNull(value, "value must not be null"));
        return this;
    }

    public JsonArray add(String value) {
        return add(new JsonString(value));
    }

    public JsonArray add(long value) {
        return add(new JsonNumber(value));
    }

    public JsonArray add(double value) {
        return add(new JsonNumber(value));
    }

    public JsonArray add(boolean value) {
        return add(JsonBoolean.of(value));
    }

    public JsonArray addNull() {
        return add(JsonNull.INSTANCE);
    }

    @Override
    public JsonArray asArray() {
        return this;
    }

    @Override
    String typeName() {
        return "array";
    }

    @Override
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        JsonWriter.writeArray(sb, this);
        return sb.toString();
    }

    List<JsonValue> elements() {
        return elements;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof JsonArray a && a.elements.equals(elements);
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }
}
