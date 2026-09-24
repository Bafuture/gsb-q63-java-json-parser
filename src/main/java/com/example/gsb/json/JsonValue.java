package com.example.gsb.json;

/**
 * JSON 值的对象模型基类。子类：{@link JsonObject}、{@link JsonArray}、
 * {@link JsonString}、{@link JsonNumber}、{@link JsonBoolean}、{@link JsonNull}。
 */
public abstract class JsonValue {

    public boolean isObject()  { return this instanceof JsonObject; }
    public boolean isArray()   { return this instanceof JsonArray; }
    public boolean isString()  { return this instanceof JsonString; }
    public boolean isNumber()  { return this instanceof JsonNumber; }
    public boolean isBoolean() { return this instanceof JsonBoolean; }
    public boolean isNull()    { return this instanceof JsonNull; }

    public JsonObject asObject() {
        throw typeError("object");
    }

    public JsonArray asArray() {
        throw typeError("array");
    }

    public String asString() {
        throw typeError("string");
    }

    public JsonNumber asNumber() {
        throw typeError("number");
    }

    public boolean asBoolean() {
        throw typeError("boolean");
    }

    private IllegalStateException typeError(String expected) {
        return new IllegalStateException(
                "not a " + expected + ": " + getClass().getSimpleName());
    }

    /** 序列化为紧凑 JSON 文本。 */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        writeJson(sb);
        return sb.toString();
    }

    abstract void writeJson(StringBuilder sb);

    @Override
    public String toString() {
        return toJson();
    }
}
