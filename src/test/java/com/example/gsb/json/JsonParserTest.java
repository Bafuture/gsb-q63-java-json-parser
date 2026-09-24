package com.example.gsb.json;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 正常解析场景。 */
class JsonParserTest {

    @Test
    @DisplayName("解析六种基本类型")
    void parseAllValueTypes() {
        JsonValue value = Json.parse("{\"s\":\"text\",\"n\":-12.5e2,\"t\":true,\"f\":false,\"z\":null,\"a\":[1,2]}");
        JsonObject obj = value.asObject();
        assertThat(obj.get("s")).isEqualTo(new JsonString("text"));
        assertThat(obj.get("n").asNumber().bigDecimalValue()).isEqualByComparingTo(new BigDecimal("-1250"));
        assertThat(obj.get("t")).isEqualTo(JsonBoolean.TRUE);
        assertThat(obj.get("f")).isEqualTo(JsonBoolean.FALSE);
        assertThat(obj.get("z")).isEqualTo(JsonNull.INSTANCE);
        assertThat(obj.get("a").asArray().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("解析嵌套对象与数组，保留键序")
    void parseNestedAndKeepOrder() {
        JsonObject obj = Json.parse("{\"b\":1,\"a\":{\"x\":[true,{\"y\":null}]},\"c\":[]}").asObject();
        assertThat(obj.members().keySet()).containsExactly("b", "a", "c");
        JsonArray x = obj.get("a").asObject().get("x").asArray();
        assertThat(x.get(0)).isEqualTo(JsonBoolean.TRUE);
        assertThat(x.get(1).asObject().get("y")).isEqualTo(JsonNull.INSTANCE);
        assertThat(obj.get("c").asArray().size()).isZero();
    }

    @Test
    @DisplayName("空对象与空数组")
    void parseEmpty() {
        assertThat(Json.parse("{}").asObject().size()).isZero();
        assertThat(Json.parse("[]").asArray().size()).isZero();
        assertThat(Json.parse("  {  }  ").asObject().size()).isZero();
    }

    @Test
    @DisplayName("字符串转义：常见控制字符")
    void parseCommonEscapes() {
        JsonString s = Json.parse("\"a\\\"b\\\\c\\/d\\be\\ff\\ng\\rh\\ti\"").asString();
        assertThat(s.value()).isEqualTo("a\"b\\c/d\be\ff\ng\rh\ti");
    }

    @Test
    @DisplayName("字符串转义：\\uXXXX 与代理项对")
    void parseUnicodeEscapes() {
        assertThat(Json.parse("\"\\u0041\\u4e2d\\u6587\"").asString().value()).isEqualTo("A中文");
        // 😀 = U+1F600，由代理项对 D83D DE00 组成
        assertThat(Json.parse("\"\\uD83D\\uDE00\"").asString().value()).isEqualTo("\uD83D\uDE00");
        assertThat(Json.parse("\"\\uD83D\\uDE00\"").asString().value().codePointAt(0)).isEqualTo(0x1F600);
    }

    @Test
    @DisplayName("数字：整数、负数、小数、指数")
    void parseNumbers() {
        assertThat(Json.parse("0").asNumber().bigDecimalValue()).isEqualByComparingTo("0");
        assertThat(Json.parse("-0.5").asNumber().bigDecimalValue()).isEqualByComparingTo("-0.5");
        assertThat(Json.parse("3.14159265358979323846").asNumber().bigDecimalValue())
                .isEqualByComparingTo("3.14159265358979323846");
        assertThat(Json.parse("1.5e3").asNumber().bigDecimalValue()).isEqualByComparingTo("1500");
        assertThat(Json.parse("-2E-2").asNumber().bigDecimalValue()).isEqualByComparingTo("-0.02");
    }

    @Test
    @DisplayName("大整数不丢精度（double 无法精确表示）")
    void bigIntegerKeepsPrecision() {
        // 2^53 + 1，double 会舍入为 9007199254740992
        JsonNumber n = Json.parse("9007199254740993").asNumber();
        assertThat(n.bigDecimalValue()).isEqualByComparingTo("9007199254740993");
        assertThat(n.bigDecimalValue().toPlainString()).isEqualTo("9007199254740993");

        // 40 位大整数
        String big = "1234567890123456789012345678901234567890";
        assertThat(Json.parse(big).asNumber().bigDecimalValue().toPlainString()).isEqualTo(big);

        // 高精度小数
        String precise = "0.123456789012345678901234567890123456789";
        assertThat(Json.parse(precise).asNumber().bigDecimalValue().toPlainString()).isEqualTo(precise);
    }

    @Test
    @DisplayName("根值可以是任意 JSON 值")
    void parseRootPrimitives() {
        assertThat(Json.parse("\"hello\"")).isEqualTo(new JsonString("hello"));
        assertThat(Json.parse("42")).isEqualTo(JsonNumber.of(42));
        assertThat(Json.parse("true")).isEqualTo(JsonBoolean.TRUE);
        assertThat(Json.parse("null")).isEqualTo(JsonNull.INSTANCE);
    }

    @Test
    @DisplayName("容忍各种空白字符")
    void parseWhitespace() {
        JsonArray arr = Json.parse("\t\r\n [ 1 ,\r\n\t2 ] ").asArray();
        assertThat(arr.size()).isEqualTo(2);
    }
}
