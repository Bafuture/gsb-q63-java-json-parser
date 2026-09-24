package com.example.gsb.json;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** 正常解析场景。 */
class JsonParseTest {

    @Test
    void parsesObjectWithAllValueTypes() {
        JsonValue value = Json.parse("""
                {
                  "name": "gsb",
                  "tags": ["a", "b"],
                  "count": 42,
                  "ratio": 0.5,
                  "enabled": true,
                  "deleted": false,
                  "extra": null
                }
                """);
        JsonObject obj = value.asObject();
        assertThat(obj.get("name").asString()).isEqualTo("gsb");
        assertThat(obj.get("tags").asArray()).hasSize(2);
        assertThat(obj.get("count").asNumber().intValue()).isEqualTo(42);
        assertThat(obj.get("ratio").asNumber().bigDecimalValue())
                .isEqualByComparingTo(new BigDecimal("0.5"));
        assertThat(obj.get("enabled").asBoolean()).isTrue();
        assertThat(obj.get("deleted").asBoolean()).isFalse();
        assertThat(obj.get("extra").isNull()).isTrue();
    }

    @Test
    void parsesNestedEmptyAndWhitespace() {
        JsonObject obj = Json.parse(" { \"a\" : [ ] , \"b\" : { } } ").asObject();
        assertThat(obj.get("a").asArray()).isEmpty();
        assertThat(obj.get("b").asObject().size()).isZero();
    }

    @Test
    void parsesStringEscapes() {
        JsonValue v = Json.parse("\"a\\nb\\t\\r\\f\\b\\\\/\"");
        assertThat(v.asString()).isEqualTo("a\nb\t\r\f\b\\/");
    }

    @Test
    void parsesUnicodeEscapesIncludingSurrogatePairs() {
        assertThat(Json.parse("\"\\u0041\\u4e2d\\u6587\"").asString())
                .isEqualTo("A中文");
        // 代理对：U+1F600 😀
        assertThat(Json.parse("\"\\ud83d\\ude00\"").asString())
                .isEqualTo("\uD83D\uDE00");
    }

    @Test
    void keepsBigIntegerPrecision() {
        // 超过 double 53 位尾数能精确表示的范围
        JsonValue v = Json.parse("9007199254740993");
        assertThat(v.asNumber().bigDecimalValue())
                .isEqualTo(new BigDecimal("9007199254740993"));
    }

    @Test
    void keepsDecimalAndExponentPrecision() {
        assertThat(Json.parse("0.1").asNumber().bigDecimalValue())
                .isEqualTo(new BigDecimal("0.1"));
        assertThat(Json.parse("1.2300e-4").asNumber().bigDecimalValue())
                .isEqualTo(new BigDecimal("1.2300e-4"));
        assertThat(Json.parse("-123456789012345678901234567890.5")
                .asNumber().bigDecimalValue())
                .isEqualTo(new BigDecimal("-123456789012345678901234567890.5"));
    }

    @Test
    void parsesTopLevelScalars() {
        assertThat(Json.parse("true").asBoolean()).isTrue();
        assertThat(Json.parse("null").isNull()).isTrue();
        assertThat(Json.parse("\"hi\"").asString()).isEqualTo("hi");
        assertThat(Json.parse("  -7  ").asNumber().intValue()).isEqualTo(-7);
    }

    @Test
    void objectKeepsInsertionOrder() {
        JsonObject obj = Json.parse("{\"z\":1,\"a\":2,\"m\":3}").asObject();
        assertThat(obj.toJson()).isEqualTo("{\"z\":1,\"a\":2,\"m\":3}");
    }
}
