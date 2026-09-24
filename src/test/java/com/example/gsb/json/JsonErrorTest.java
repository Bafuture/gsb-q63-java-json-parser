package com.example.gsb.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 错误定位场景：行号、列号与可读提示。 */
class JsonErrorTest {

    @Test
    @DisplayName("缺少冒号：报出准确行列号与提示")
    void missingColon() {
        assertThatThrownBy(() -> Json.parse("{\"a\" 1}"))
                .isInstanceOfSatisfying(JsonParseException.class, e -> {
                    assertThat(e.getLine()).isEqualTo(1);
                    assertThat(e.getColumn()).isEqualTo(6);
                    assertThat(e.getMessage()).contains("第 1 行第 6 列").contains("':'");
                });
    }

    @Test
    @DisplayName("多行文本中的错误：正确定位到第 3 行")
    void errorInMultiline() {
        String source = "{\n  \"a\": 1,\n  \"b\": tru\n}";
        assertThatThrownBy(() -> Json.parse(source))
                .isInstanceOfSatisfying(JsonParseException.class, e -> {
                    assertThat(e.getLine()).isEqualTo(3);
                    assertThat(e.getColumn()).isEqualTo(8);
                    assertThat(e.getMessage()).contains("true");
                });
    }

    @Test
    @DisplayName("尾随逗号：对象与数组都报错")
    void trailingComma() {
        assertThatThrownBy(() -> Json.parse("{\"a\":1,}"))
                .isInstanceOf(JsonParseException.class)
                .hasMessageContaining("尾随逗号");
        assertThatThrownBy(() -> Json.parse("[1,2,]"))
                .isInstanceOf(JsonParseException.class)
                .hasMessageContaining("尾随逗号");
    }

    @Test
    @DisplayName("非法数字：前导零、裸小数点、缺指数")
    void invalidNumbers() {
        assertThatThrownBy(() -> Json.parse("01"))
                .isInstanceOf(JsonParseException.class)
                .hasMessageContaining("前导零");
        assertThatThrownBy(() -> Json.parse("[1.]"))
                .isInstanceOf(JsonParseException.class)
                .hasMessageContaining("小数点");
        assertThatThrownBy(() -> Json.parse("[1e]"))
                .isInstanceOf(JsonParseException.class)
                .hasMessageContaining("指数");
        assertThatThrownBy(() -> Json.parse("-"))
                .isInstanceOf(JsonParseException.class)
                .hasMessageContaining("数字不完整");
    }

    @Test
    @DisplayName("字符串未结束与非法转义")
    void badStrings() {
        assertThatThrownBy(() -> Json.parse("\"abc"))
                .isInstanceOf(JsonParseException.class)
                .hasMessageContaining("字符串未结束");
        assertThatThrownBy(() -> Json.parse("\"a\\xb\""))
                .isInstanceOf(JsonParseException.class)
                .hasMessageContaining("非法转义序列");
        assertThatThrownBy(() -> Json.parse("\"a\\u12G4\""))
                .isInstanceOf(JsonParseException.class)
                .hasMessageContaining("十六进制");
        // 未转义的换行属于控制字符
        assertThatThrownBy(() -> Json.parse("\"a\nb\""))
                .isInstanceOf(JsonParseException.class)
                .hasMessageContaining("控制字符");
    }

    @Test
    @DisplayName("代理项配对无效")
    void badSurrogate() {
        assertThatThrownBy(() -> Json.parse("\"\\uD83Dx\""))
                .isInstanceOf(JsonParseException.class)
                .hasMessageContaining("代理项");
        assertThatThrownBy(() -> Json.parse("\"\\uDE00\""))
                .isInstanceOf(JsonParseException.class)
                .hasMessageContaining("代理项");
    }

    @Test
    @DisplayName("非法字符与残缺字面量")
    void badTokens() {
        assertThatThrownBy(() -> Json.parse("{\"a\": @}"))
                .isInstanceOf(JsonParseException.class)
                .hasMessageContaining("非法字符");
        assertThatThrownBy(() -> Json.parse("nul"))
                .isInstanceOf(JsonParseException.class)
                .hasMessageContaining("null");
    }

    @Test
    @DisplayName("根值后有多余内容")
    void trailingContent() {
        assertThatThrownBy(() -> Json.parse("true false"))
                .isInstanceOfSatisfying(JsonParseException.class, e -> {
                    assertThat(e.getLine()).isEqualTo(1);
                    assertThat(e.getColumn()).isEqualTo(6);
                    assertThat(e.getMessage()).contains("多余内容");
                });
    }

    @Test
    @DisplayName("结构未闭合：报错位置在输入末尾")
    void unclosed() {
        assertThatThrownBy(() -> Json.parse("{\"a\": [1, 2"))
                .isInstanceOfSatisfying(JsonParseException.class, e -> {
                    assertThat(e.getLine()).isEqualTo(1);
                    assertThat(e.getColumn()).isEqualTo(12);
                    assertThat(e.getMessage()).contains("EOF");
                });
    }

    @Test
    @DisplayName("空输入")
    void emptyInput() {
        assertThatThrownBy(() -> Json.parse("   "))
                .isInstanceOf(JsonParseException.class)
                .hasMessageContaining("JSON 值");
    }

    @Test
    @DisplayName("嵌套过深被拒绝")
    void tooDeep() {
        String deep = "[".repeat(JsonParser.MAX_DEPTH + 2) + "1" + "]".repeat(JsonParser.MAX_DEPTH + 2);
        assertThatThrownBy(() -> Json.parse(deep))
                .isInstanceOf(JsonParseException.class)
                .hasMessageContaining("嵌套层级过深");
    }
}
