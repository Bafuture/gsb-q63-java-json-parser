package com.example.gsb.json;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** JSON object node preserving member insertion order. */
public final class JsonObject extends JsonValue {

    private final Map<String, JsonValue> members = new LinkedHashMap<>();

    public int size() {
        return members.size();
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public boolean containsKey(String key) {
        return members.containsKey(key);
    }

    public Set<String> keys() {
        return members.keySet();
    }

    public JsonValue get(String key) {
        return members.get(key);
    }

    public JsonObject set(String key, JsonValue value) {
        members.put(Objects.requireNonNull(key, "key must not be null"),
                Objects.requireNonNull(value, "value must not be null"));
        return this;
    }

    public JsonObject set(String key, String value) {
        return set(key, new JsonString(value));
    }

    public JsonObject set(String key, long value) {
        return set(key, new JsonNumber(value));
    }

    public JsonObject set(String key, double value) {
        return set(key, new JsonNumber(value));
    }

    public JsonObject set(String key, boolean value) {
        return set(key, JsonBoolean.of(value));
    }

    public JsonObject setNull(String key) {
        return set(key, JsonNull.INSTANCE);
    }

    @Override
    public JsonObject asObject() {
        return this;
    }

    @Override
    String typeName() {
        return "object";
    }

    @Override
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        JsonWriter.writeObject(sb, this);
        return sb.toString();
    }

    Map<String, JsonValue> members() {
        return members;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof JsonObject o && o.members.equals(members);
    }

    @Override
    public int hashCode() {
        return members.hashCode();
    }
}
