package com.example.gsb.json;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * JSON 对象。内部使用 {@link LinkedHashMap}，保留键的插入顺序，
 * 保证「解析 → 序列化」后键序不变。
 */
public final class JsonObject implements JsonValue {

    private final LinkedHashMap<String, JsonValue> members = new LinkedHashMap<>();

    public JsonObject put(String key, JsonValue value) {
        members.put(Objects.requireNonNull(key, "key 不能为 null"),
                Objects.requireNonNull(value, "value 不能为 null，请使用 JsonNull.INSTANCE"));
        return this;
    }

    public JsonObject put(String key, String value) {
        return put(key, new JsonString(value));
    }

    public JsonObject put(String key, java.math.BigDecimal value) {
        return put(key, new JsonNumber(value));
    }

    public JsonObject put(String key, boolean value) {
        return put(key, JsonBoolean.of(value));
    }

    public JsonValue get(String key) {
        return members.get(key);
    }

    public boolean containsKey(String key) {
        return members.containsKey(key);
    }

    public int size() {
        return members.size();
    }

    /** 只读视图，键序与插入顺序一致。 */
    public Map<String, JsonValue> members() {
        return Collections.unmodifiableMap(members);
    }

    @Override
    public JsonObject asObject() {
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
        return o instanceof JsonObject other && members.equals(other.members);
    }

    @Override
    public int hashCode() {
        return members.hashCode();
    }

    @Override
    public String toString() {
        return toJson();
    }
}
