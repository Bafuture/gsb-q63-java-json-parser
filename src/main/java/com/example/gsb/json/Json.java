package com.example.gsb.json;

/**
 * 门面类：解析与序列化的统一入口。
 *
 * <pre>
 * JsonValue v = Json.parse("{\"a\": [1, 2.5, null]}");
 * String s = Json.stringify(v);
 * </pre>
 */
public final class Json {

    private Json() {
    }

    /** 解析 JSON 文本，语法错误抛出带行列号的 {@link JsonException}。 */
    public static JsonValue parse(String input) {
        return JsonParser.parse(input);
    }

    /** 序列化为紧凑 JSON 文本。 */
    public static String stringify(JsonValue value) {
        return value.toJson();
    }
}
