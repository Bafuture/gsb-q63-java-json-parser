package com.example.gsb.json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/** JSON 数组。 */
public final class JsonArray extends JsonValue implements Iterable<JsonValue> {

    private final List<JsonValue> elements = new ArrayList<>();

    public JsonArray add(JsonValue value) {
        elements.add(Objects.requireNonNull(value, "value"));
        return this;
    }

    public JsonArray add(String value) {
        return add(new JsonString(value));
    }

    public JsonArray add(long value) {
        return add(new JsonNumber(java.math.BigDecimal.valueOf(value)));
    }

    public JsonArray add(boolean value) {
        return add(JsonBoolean.of(value));
    }

    public JsonValue get(int index) {
        return elements.get(index);
    }

    @Override
    public JsonArray asArray() {
        return this;
    }

    public int size() {
        return elements.size();
    }

    @Override
    public Iterator<JsonValue> iterator() {
        return elements.iterator();
    }

    @Override
    void writeJson(StringBuilder sb) {
        sb.append('[');
        boolean first = true;
        for (JsonValue v : elements) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            v.writeJson(sb);
        }
        sb.append(']');
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof JsonArray other && elements.equals(other.elements);
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }
}
