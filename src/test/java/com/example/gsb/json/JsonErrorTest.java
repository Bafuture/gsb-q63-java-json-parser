package com.example.gsb.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/** 错误定位场景：行号、列号与可读提示。 */
class JsonErrorTest {

    @Test
    void reportsLineAndColumn() {
        assertThatThrownBy(() -> Json.parse("{\n  \"a\": 1,\n  \"b\": tru\n}"))
                .isInstanceOfSatisfying(JsonException.class, e -> {
                    assertThat(e.getLine()).isEqualTo(3);
                    assertThat(e.getColumn()).isEqualTo(8);
                    assertThat(e.getMessage())
                            .contains("line 3, column 8")
                            .contains("invalid literal");
                });
    }

    @Test
    void reportsMissingColon() {
        assertThatThrownBy(() -> Json.parse("{\"a\" 1}"))
                .isInstanceOfSatisfying(JsonException.class, e -> {
                    assertThat(e.getLine()).isEqualTo(1);
                    assertThat(e.getColumn()).isEqualTo(6);
                    assertThat(e.getMessage()).contains("expected ':'");
                });
    }

    @Test
    void reportsUnterminatedString() {
        assertThatThrownBy(() -> Json.parse("[\"abc"))
                .isInstanceOfSatisfying(JsonException.class, e ->
                        assertThat(e.getMessage()).contains("unterminated string"));
    }

    @Test
    void reportsInvalidEscape() {
        assertThatThrownBy(() -> Json.parse("\"a\\x\""))
                .isInstanceOfSatisfying(JsonException.class, e -> {
                    assertThat(e.getColumn()).isEqualTo(4);
                    assertThat(e.getMessage()).contains("invalid escape sequence");
                });
    }

    @Test
    void reportsBadUnicodeEscape() {
        assertThatThrownBy(() -> Json.parse("\"\\u12xz\""))
                .isInstanceOfSatisfying(JsonException.class, e ->
                        assertThat(e.getMessage()).contains("invalid hex digit"));
    }

    @Test
    void reportsUnescapedControlCharacter() {
        assertThatThrownBy(() -> Json.parse("\"a\nb\""))
                .isInstanceOfSatisfying(JsonException.class, e ->
                        assertThat(e.getMessage()).contains("unescaped control character"));
    }

    @Test
    void reportsLeadingZero() {
        assertThatThrownBy(() -> Json.parse("012"))
                .isInstanceOfSatisfying(JsonException.class, e ->
                        assertThat(e.getMessage()).contains("leading zeros"));
    }

    @Test
    void reportsMissingFractionDigits() {
        assertThatThrownBy(() -> Json.parse("1."))
                .isInstanceOfSatisfying(JsonException.class, e ->
                        assertThat(e.getMessage()).contains("expected a digit after '.'"));
    }

    @Test
    void reportsTrailingCommaInArray() {
        assertThatThrownBy(() -> Json.parse("[1,]"))
                .isInstanceOfSatisfying(JsonException.class, e -> {
                    assertThat(e.getColumn()).isEqualTo(4);
                    assertThat(e.getMessage()).contains("expected a value");
                });
    }

    @Test
    void reportsTrailingContentAfterTopLevelValue() {
        assertThatThrownBy(() -> Json.parse("{} []"))
                .isInstanceOfSatisfying(JsonException.class, e -> {
                    assertThat(e.getColumn()).isEqualTo(4);
                    assertThat(e.getMessage()).contains("after top-level value");
                });
    }

    @Test
    void reportsUnexpectedEof() {
        assertThatThrownBy(() -> Json.parse("{\"a\":"))
                .isInstanceOfSatisfying(JsonException.class, e ->
                        assertThat(e.getMessage()).contains("end of input"));
    }

    @Test
    void reportsExcessiveNesting() {
        String deep = "[".repeat(1001) + "1" + "]".repeat(1001);
        assertThatThrownBy(() -> Json.parse(deep))
                .isInstanceOfSatisfying(JsonException.class, e ->
                        assertThat(e.getMessage()).contains("nesting too deep"));
    }
}
