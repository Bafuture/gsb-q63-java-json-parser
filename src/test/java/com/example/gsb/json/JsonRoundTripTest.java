package com.example.gsb.json;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** 往返一致场景：解析 → 序列化 → 再解析，结果等价。 */
class JsonRoundTripTest {

    private static final String COMPLEX_DOC = """
            {
              "orderId": 9007199254740993,
              "amount": 0.1,
              "tags": ["生鲜", "次日达", "含\\"引号\\"与\\\\反斜杠"],
              "address": {"city": "上海", "zip": "200000", "geo": null},
              "paid": true,
              "discount": 1.5e-3,
              "emoji": "\\uD83D\\uDE00",
              "empty": {},
              "items": []
            }
            """;

    @Test
    @DisplayName("复杂文档：紧凑序列化往返等价")
    void roundTripCompact() {
        JsonValue first = Json.parse(COMPLEX_DOC);
        String serialized = Json.stringify(first);
        JsonValue second = Json.parse(serialized);
        assertThat(second).isEqualTo(first);
        // 再次序列化，文本也完全一致（定点）
        assertThat(Json.stringify(second)).isEqualTo(serialized);
    }

    @Test
    @DisplayName("复杂文档：美化序列化往返等价")
    void roundTripPretty() {
        JsonValue first = Json.parse(COMPLEX_DOC);
        String pretty = Json.stringify(first, true);
        assertThat(pretty).contains("\n").contains("  ");
        assertThat(Json.parse(pretty)).isEqualTo(first);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0", "-0", "1", "-1", "3.14", "0.1", "1e2", "1E+2", "1.5e-3",
            "9007199254740993",
            "1234567890123456789012345678901234567890",
            "0.123456789012345678901234567890123456789",
            "1.0", "1.00"
    })
    @DisplayName("数字：序列化文本可被再次解析且完全相等（含标度）")
    void numberRoundTrip(String literal) {
        JsonNumber first = Json.parse(literal).asNumber();
        String serialized = first.toJson();
        JsonNumber second = Json.parse(serialized).asNumber();
        // BigDecimal.equals 区分标度，相等即代表精度与标度都未丢失
        assertThat(second).isEqualTo(first);
        assertThat(second.toJson()).isEqualTo(serialized);
    }

    @Test
    @DisplayName("字符串：所有转义字符往返一致")
    void stringEscapeRoundTrip() {
        String raw = "引号\"反斜杠\\换行\n制表\t回车\r退格\b换页\f控制符中文😀";
        JsonString first = new JsonString(raw);
        String serialized = first.toJson();
        // 控制字符必须被转义，序列化结果中不允许出现裸控制字符
        assertThat(serialized.chars().filter(c -> c < 0x20)).isEmpty();
        assertThat(Json.parse(serialized)).isEqualTo(first);
    }

    @Test
    @DisplayName("对象键序在往返后保持不变")
    void keyOrderPreserved() {
        JsonValue value = Json.parse("{\"z\":1,\"m\":2,\"a\":3}");
        assertThat(Json.stringify(value)).isEqualTo("{\"z\":1,\"m\":2,\"a\":3}");
    }

    @Test
    @DisplayName("手工构建的对象模型可序列化并解析回等价模型")
    void buildThenRoundTrip() {
        JsonObject obj = new JsonObject()
                .put("name", "订单")
                .put("id", JsonNumber.of(java.math.BigInteger.valueOf(Long.MAX_VALUE).add(java.math.BigInteger.ONE)))
                .put("ok", true)
                .put("note", JsonNull.INSTANCE)
                .put("list", new JsonArray().add("a").add(false).add(new JsonObject().put("k", "v")));
        JsonValue reparsed = Json.parse(Json.stringify(obj));
        assertThat(reparsed).isEqualTo(obj);
    }

    @Test
    @DisplayName("空对象与空数组的序列化形式")
    void emptyForms() {
        assertThat(Json.stringify(Json.parse("{}"))).isEqualTo("{}");
        assertThat(Json.stringify(Json.parse("[]"))).isEqualTo("[]");
        assertThat(Json.stringify(Json.parse("{\"a\":{},\"b\":[]}"))).isEqualTo("{\"a\":{},\"b\":[]}");
    }
}
