package com.example.gsb.json;

/**
 * JSON 解析与序列化的统一入口。
 *
 * <pre>{@code
 * JsonValue value = Json.parse("{\"a\": [1, 2, 3]}");
 * String compact = Json.stringify(value);        // {"a":[1,2,3]}
 * String pretty  = Json.stringify(value, true);  // 带缩进的美化输出
 * }</pre>
 */
public final class Json {

    private Json() {
    }

    /**
     * 解析 JSON 文本为对象模型。
     *
     * @throws JsonParseException 语法错误，异常消息含行号、列号与可读提示
     */
    public static JsonValue parse(String source) {
        if (source == null) {
            throw new IllegalArgumentException("输入不能为 null");
        }
        return new JsonParser(source).parse();
    }

    /** 序列化为紧凑 JSON 文本。 */
    public static String stringify(JsonValue value) {
        StringBuilder sb = new StringBuilder();
        JsonWriter.write(value, sb, -1, 0);
        return sb.toString();
    }

    /** 序列化；pretty 为 true 时使用 2 空格缩进的美化格式。 */
    public static String stringify(JsonValue value, boolean pretty) {
        StringBuilder sb = new StringBuilder();
        JsonWriter.write(value, sb, pretty ? 2 : -1, 0);
        return sb.toString();
    }
}
