package com.example.gsb.json;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RoundTripTest {

    private static final String DOCUMENT = """
            {
              "service": "payment-gateway",
              "version": 3,
              "enabled": true,
              "deprecated": false,
              "owner": null,
              "limits": {
                "maxAmount": 99999999999999999999999999.00000001,
                "bigId": 9007199254740993,
                "ratio": 0.1,
                "exponent": 6.02e23
              },
              "endpoints": [
                {"path": "/pay", "methods": ["POST"]},
                {"path": "/refund", "methods": ["POST", "GET"]}
              ],
              "greeting": "Hello, 世界! \\n \\"quoted\\" 😀",
              "emptyObject": {},
              "emptyArray": []
            }
            """;

    @Test
    void parseSerializeParseYieldsEqualTree() {
        JsonValue first = JsonParser.parse(DOCUMENT);
        String serialized = first.toJson();
        JsonValue second = JsonParser.parse(serialized);

        assertThat(second).isEqualTo(first);
        assertThat(second.toJson()).isEqualTo(serialized);
    }

    @Test
    void serializedOutputIsStable() {
        JsonValue value = JsonParser.parse(DOCUMENT);
        assertThat(value.toJson()).isEqualTo(JsonParser.parse(value.toJson()).toJson());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0", "-0.0", "1", "-1", "3.14", "1e3", "1E+3", "2.5e-7",
            "9007199254740993", "-9007199254740993",
            "123456789012345678901234567890",
            "0.00000000000000000000000000001",
            "1.7976931348623157e308"
    })
    void numbersSurviveRoundTrip(String lexeme) {
        JsonValue value = JsonParser.parse(lexeme);
        JsonValue reparsed = JsonParser.parse(value.toJson());
        assertThat(reparsed).isEqualTo(value);
        assertThat(reparsed.asNumber()).isEqualByComparingTo(new BigDecimal(lexeme));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "\"\"", "\"plain\"", "\"with \\\"quotes\\\" and \\\\ backslash\"",
            "\"tab\\tnewline\\ncarriage\\r\"", "\"\\u0000\\u001f\"",
            "\"unicode: \\u4e2d\\u6587 \\uD83D\\uDE00\""
    })
    void stringsSurviveRoundTrip(String literal) {
        JsonValue value = JsonParser.parse(literal);
        JsonValue reparsed = JsonParser.parse(value.toJson());
        assertThat(reparsed).isEqualTo(value);
        assertThat(reparsed.asString()).isEqualTo(value.asString());
    }

    @Test
    void programmaticallyBuiltTreeSerializesAndParsesBack() {
        JsonObject object = new JsonObject()
                .set("id", new JsonNumber(new BigDecimal("123456789012345678901234567890")))
                .set("name", "gsb")
                .set("active", true)
                .setNull("deletedAt")
                .set("scores", new JsonArray().add(1).add(2.5).add("three").add(false).addNull());

        JsonValue reparsed = JsonParser.parse(object.toJson());
        assertThat(reparsed).isEqualTo(object);
    }

    @Test
    void numericEqualityIgnoresScale() {
        assertThat(JsonParser.parse("1.0")).isEqualTo(JsonParser.parse("1"));
        assertThat(JsonParser.parse("1.0").hashCode()).isEqualTo(JsonParser.parse("1").hashCode());
    }
}
