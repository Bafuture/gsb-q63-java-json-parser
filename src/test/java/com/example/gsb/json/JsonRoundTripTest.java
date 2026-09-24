package com.example.gsb.json;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** 往返一致场景：parse → stringify → parse 结果等价。 */
class JsonRoundTripTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "[]",
            "{\"a\":1,\"b\":[true,false,null],\"c\":{\"d\":\"e\"}}",
            "\"\\u0041\\u4e2d\\n\\t\\\"\"",
            "9007199254740993",
            "0.1",
            "1.2300",
            "1E+3",
            "-0.5e-10",
            "[[[1],[2,3]],{\"x\":{}}]",
    })
    void roundTripPreservesValue(String json) {
        JsonValue first = Json.parse(json);
        String serialized = first.toJson();
        JsonValue second = Json.parse(serialized);
        assertThat(second).isEqualTo(first);
        assertThat(second.toJson()).isEqualTo(serialized);
    }

    @Test
    void roundTripOnComplexDocument() {
        String json = """
                {"orderId":9007199254740993,"amount":0.1,"items":[
                {"sku":"A-1","qty":2,"price":19.99},
                {"sku":"B-2","qty":1,"price":0.30000000000000004}],
                "paid":true,"coupon":null,"note":"换行\\n与\\"引号\\""}""";
        JsonValue first = Json.parse(json);
        JsonValue second = Json.parse(first.toJson());
        assertThat(second).isEqualTo(first);
        // 大整数与高精度小数在序列化后仍逐位一致
        assertThat(second.asObject().get("orderId").asNumber().bigDecimalValue()
                .toPlainString()).isEqualTo("9007199254740993");
        assertThat(second.asObject().get("amount").asNumber().bigDecimalValue()
                .toPlainString()).isEqualTo("0.1");
    }

    @Test
    void serializesControlCharactersAsEscapes() {
        JsonObject obj = new JsonObject().put("k", "a\nb\tc\"d\\ef");
        assertThat(obj.toJson()).isEqualTo("{\"k\":\"a\\nb\\tc\\\"d\\\\e\\u0001f\"}");
        assertThat(Json.parse(obj.toJson())).isEqualTo(obj);
    }

    @Test
    void buildsAndSerializesProgrammatically() {
        JsonObject obj = new JsonObject()
                .put("name", "gsb")
                .put("n", 7L)
                .put("ok", true)
                .put("nil", JsonNull.INSTANCE)
                .put("arr", new JsonArray().add(1L).add("x").add(false));
        assertThat(Json.parse(obj.toJson())).isEqualTo(obj);
    }
}
