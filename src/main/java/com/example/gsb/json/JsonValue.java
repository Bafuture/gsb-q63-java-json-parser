package com.example.gsb.json;

/**
 * JSON 值的对象模型根接口。sealed 类型，只允许以下六种实现：
 * {@link JsonObject}、{@link JsonArray}、{@link JsonString}、
 * {@link JsonNumber}、{@link JsonBoolean}、{@link JsonNull}。
 */
public sealed interface JsonValue
        permits JsonObject, JsonArray, JsonString, JsonNumber, JsonBoolean, JsonNull {

    /** 序列化为紧凑 JSON 文本（无多余空白）。 */
    String toJson();

    /** 序列化为带缩进的美化 JSON 文本。 */
    String toPrettyJson();

    default boolean isObject() {
        return this instanceof JsonObject;
    }

    default boolean isArray() {
        return this instanceof JsonArray;
    }

    default boolean isString() {
        return this instanceof JsonString;
    }

    default boolean isNumber() {
        return this instanceof JsonNumber;
    }

    default boolean isBoolean() {
        return this instanceof JsonBoolean;
    }

    default boolean isNull() {
        return this instanceof JsonNull;
    }

    default JsonObject asObject() {
        throw new IllegalStateException("当前值不是对象：" + getClass().getSimpleName());
    }

    default JsonArray asArray() {
        throw new IllegalStateException("当前值不是数组：" + getClass().getSimpleName());
    }

    default JsonString asString() {
        throw new IllegalStateException("当前值不是字符串：" + getClass().getSimpleName());
    }

    default JsonNumber asNumber() {
        throw new IllegalStateException("当前值不是数字：" + getClass().getSimpleName());
    }

    default JsonBoolean asBoolean() {
        throw new IllegalStateException("当前值不是布尔值：" + getClass().getSimpleName());
    }
}
