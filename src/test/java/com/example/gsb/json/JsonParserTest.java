package com.example.gsb.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class JsonParserTest {

    @Test
    void parsesNestedObjectWithAllValueTypes() {
        JsonObject root = JsonParser.parseObject("""
                {
                  "name": "order-service",
                  "port": 8080,
                  "debug": true,
                  "fallback": null,
                  "tags": ["json", "rpc"],
                  "limits": {"cpu": 1.5, "memory": -2048}
                }
                """);

        assertThat(root.get("name").asString()).isEqualTo("order-service");
        assertThat(root.get("port").asNumber()).isEqualByComparingTo(new BigDecimal("8080"));
        assertThat(root.get("debug").asBoolean()).isTrue();
        assertThat(root.get("fallback").isNull()).isTrue();

        JsonArray tags = root.get("tags").asArray();
        assertThat(tags.size()).isEqualTo(2);
        assertThat(tags.get(0).asString()).isEqualTo("json");

        JsonObject limits = root.get("limits").asObject();
        assertThat(limits.get("cpu").asNumber()).isEqualByComparingTo(new BigDecimal("1.5"));
        assertThat(limits.get("memory").asNumber()).isEqualByComparingTo(new BigDecimal("-2048"));
    }

    @Test
    void parsesEmptyObjectAndArray() {
        assertThat(JsonParser.parseObject("{}").isEmpty()).isTrue();
        assertThat(JsonParser.parseArray("[]").isEmpty()).isTrue();
        assertThat(JsonParser.parseObject("  {  }  ").isEmpty()).isTrue();
    }

    @Test
    void parsesStringEscapes() {
        assertThat(JsonParser.parse("\"\\n\"").asString()).isEqualTo("\n");
        assertThat(JsonParser.parse("\"\\t\"").asString()).isEqualTo("\t");
        assertThat(JsonParser.parse("\"\\r\"").asString()).isEqualTo("\r");
        assertThat(JsonParser.parse("\"\\f\"").asString()).isEqualTo("\f");
        assertThat(JsonParser.parse("\"\\b\"").asString()).isEqualTo("\b");
        assertThat(JsonParser.parse("\"\\\\\"").asString()).isEqualTo("\\");
        assertThat(JsonParser.parse("\"\\/\"").asString()).isEqualTo("/");
        assertThat(JsonParser.parse("\"\\\"\"").asString()).isEqualTo("\"");
        assertThat(JsonParser.parse("\"a\\nb\\tend\"").asString()).isEqualTo("a\nb\tend");
    }

    @Test
    void parsesUnicodeEscapes() {
        assertThat(JsonParser.parse("\"\\u0041\\u4e2d\\u6587\"").asString()).isEqualTo("A中文");
        assertThat(JsonParser.parse("\"\\u00e9\"").asString()).isEqualTo("é");
    }

    @Test
    void parsesSurrogatePairs() {
        // U+1F600 GRINNING FACE
        assertThat(JsonParser.parse("\"\\uD83D\\uDE00\"").asString()).isEqualTo("\uD83D\uDE00");
    }

    @Test
    void keepsBigIntegerPrecision() {
        // 2^53 + 1 cannot be represented exactly as a double.
        JsonValue value = JsonParser.parse("9007199254740993");
        assertThat(value.asNumber()).isEqualByComparingTo(new BigDecimal("9007199254740993"));
    }

    @Test
    void keepsDecimalPrecision() {
        String lexeme = "3.1415926535897932384626433832795028841971";
        JsonValue value = JsonParser.parse(lexeme);
        assertThat(value.asNumber()).isEqualByComparingTo(new BigDecimal(lexeme));
        assertThat(value.toJson()).isEqualTo(lexeme);
    }

    @Test
    void parsesExponentsAndSignedNumbers() {
        assertThat(JsonParser.parse("1e3").asNumber()).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(JsonParser.parse("-2.5E-2").asNumber()).isEqualByComparingTo(new BigDecimal("-0.025"));
        assertThat(JsonParser.parse("0").asNumber()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(JsonParser.parse("-0.0").asNumber()).isEqualByComparingTo(new BigDecimal("0.0"));
    }

    @Test
    void parsesTopLevelScalars() {
        assertThat(JsonParser.parse("true").asBoolean()).isTrue();
        assertThat(JsonParser.parse("false").asBoolean()).isFalse();
        assertThat(JsonParser.parse("null").isNull()).isTrue();
        assertThat(JsonParser.parse("  \"hello\"  ").asString()).isEqualTo("hello");
    }

    @Test
    void rejectsNullInput() {
        assertThatThrownBy(() -> JsonParser.parse(null))
                .isInstanceOf(NullPointerException.class);
    }
}
