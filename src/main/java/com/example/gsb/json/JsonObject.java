package com.example.gsb.json;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** JSON 对象，保持键的插入顺序。 */
public final class JsonObject extends JsonValue {

    private final LinkedHashMap<String, JsonValue> members = new LinkedHashMap<>();

    public JsonObject put(String key, JsonValue value) {
        members.put(Objects.requireNonNull(key, "key"),
                Objects.requireNonNull(value, "value"));
        return this;
    }

    public JsonObject put(String key, String value) {
        return put(key, new JsonString(value));
    }

    public JsonObject put(String key, java.math.BigDecimal value) {
        return put(key, new JsonNumber(value));
    }

    public JsonObject put(String key, long value) {
        return put(key, new JsonNumber(java.math.BigDecimal.valueOf(value)));
    }

    public JsonObject put(String key, boolean value) {
        return put(key, JsonBoolean.of(value));
    }

    public JsonValue get(String key) {
        return members.get(key);
    }

    @Override
    public JsonObject asObject() {
        return this;
    }

    public boolean has(String key) {
        return members.containsKey(key);
    }

    public int size() {
        return members.size();
    }

    public Set<Map.Entry<String, JsonValue>> entrySet() {
        return members.entrySet();
    }

    @Override
    void writeJson(StringBuilder sb) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, JsonValue> e : members.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            JsonWriter.writeString(e.getKey(), sb);
            sb.append(':');
            e.getValue().writeJson(sb);
        }
        sb.append('}');
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof JsonObject other && members.equals(other.members);
    }

    @Override
    public int hashCode() {
        return members.hashCode();
    }
}
