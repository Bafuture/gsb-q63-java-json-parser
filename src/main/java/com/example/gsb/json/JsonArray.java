package com.example.gsb.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * JSON 数组，保留元素顺序。
 */
public final class JsonArray implements JsonValue {

    private final List<JsonValue> elements = new ArrayList<>();

    public JsonArray add(JsonValue value) {
        elements.add(Objects.requireNonNull(value, "value 不能为 null，请使用 JsonNull.INSTANCE"));
        return this;
    }

    public JsonArray add(String value) {
        return add(new JsonString(value));
    }

    public JsonArray add(java.math.BigDecimal value) {
        return add(new JsonNumber(value));
    }

    public JsonArray add(boolean value) {
        return add(JsonBoolean.of(value));
    }

    public JsonValue get(int index) {
        return elements.get(index);
    }

    public int size() {
        return elements.size();
    }

    public List<JsonValue> elements() {
        return Collections.unmodifiableList(elements);
    }

    @Override
    public JsonArray asArray() {
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
        StringBuilder sb = new StringBuilder();
        JsonWriter.write(this, sb, 2, 0);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof JsonArray other && elements.equals(other.elements);
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    @Override
    public String toString() {
        return toJson();
    }
}
