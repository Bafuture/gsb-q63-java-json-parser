package com.example.gsb.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class JsonErrorTest {

    @Test
    void reportsLineAndColumnForUnexpectedCharacter() {
        assertThatThrownBy(() -> JsonParser.parse("{\n  \"a\": @\n}"))
                .isInstanceOfSatisfying(JsonParseException.class, e -> {
                    assertThat(e.getLine()).isEqualTo(2);
                    assertThat(e.getColumn()).isEqualTo(8);
                    assertThat(e.getMessage()).contains("Unexpected character '@'");
                    assertThat(e.getMessage()).contains("line 2, column 8");
                });
    }

    @Test
    void reportsMissingColon() {
        assertThatThrownBy(() -> JsonParser.parse("{\"a\" 1}"))
                .isInstanceOfSatisfying(JsonParseException.class, e -> {
                    assertThat(e.getLine()).isEqualTo(1);
                    assertThat(e.getColumn()).isEqualTo(6);
                    assertThat(e.getMessage()).contains("Expected ':' after object key \"a\"");
                });
    }

    @Test
    void reportsUnterminatedString() {
        assertThatThrownBy(() -> JsonParser.parse("{\"a\": \"oops"))
                .isInstanceOfSatisfying(JsonParseException.class, e ->
                        assertThat(e.getMessage()).contains("Unterminated string literal"));
    }

    @Test
    void reportsTrailingComma() {
        assertThatThrownBy(() -> JsonParser.parse("[1, 2,]"))
                .isInstanceOfSatisfying(JsonParseException.class, e -> {
                    assertThat(e.getLine()).isEqualTo(1);
                    assertThat(e.getColumn()).isEqualTo(7);
                    assertThat(e.getMessage()).contains("Trailing comma in array");
                });
        assertThatThrownBy(() -> JsonParser.parse("{\"a\": 1,}"))
                .isInstanceOfSatisfying(JsonParseException.class, e ->
                        assertThat(e.getMessage()).contains("Trailing comma in object"));
    }

    @Test
    void reportsInvalidNumberForms() {
        assertThatThrownBy(() -> JsonParser.parse("01"))
                .isInstanceOfSatisfying(JsonParseException.class, e ->
                        assertThat(e.getMessage()).contains("Leading zeros"));
        assertThatThrownBy(() -> JsonParser.parse("1."))
                .isInstanceOfSatisfying(JsonParseException.class, e ->
                        assertThat(e.getMessage()).contains("decimal point"));
        assertThatThrownBy(() -> JsonParser.parse("1e"))
                .isInstanceOfSatisfying(JsonParseException.class, e ->
                        assertThat(e.getMessage()).contains("exponent"));
        assertThatThrownBy(() -> JsonParser.parse("-"))
                .isInstanceOfSatisfying(JsonParseException.class, e ->
                        assertThat(e.getMessage()).contains("expected a digit"));
    }

    @Test
    void reportsInvalidEscapeAndControlCharacters() {
        assertThatThrownBy(() -> JsonParser.parse("\"\\x\""))
                .isInstanceOfSatisfying(JsonParseException.class, e ->
                        assertThat(e.getMessage()).contains("Invalid escape sequence"));
        assertThatThrownBy(() -> JsonParser.parse("\"\\u12G4\""))
                .isInstanceOfSatisfying(JsonParseException.class, e ->
                        assertThat(e.getMessage()).contains("Invalid hex digit"));
        assertThatThrownBy(() -> JsonParser.parse("\"a\tb\""))
                .isInstanceOfSatisfying(JsonParseException.class, e ->
                        assertThat(e.getMessage()).contains("Unescaped control character"));
    }

    @Test
    void reportsUnpairedSurrogates() {
        assertThatThrownBy(() -> JsonParser.parse("\"\\uD83Dabc\""))
                .isInstanceOfSatisfying(JsonParseException.class, e ->
                        assertThat(e.getMessage()).contains("Unpaired high surrogate"));
        assertThatThrownBy(() -> JsonParser.parse("\"\\uDE00\""))
                .isInstanceOfSatisfying(JsonParseException.class, e ->
                        assertThat(e.getMessage()).contains("Unpaired low surrogate"));
    }

    @Test
    void reportsTrailingContent() {
        assertThatThrownBy(() -> JsonParser.parse("{} {}"))
                .isInstanceOfSatisfying(JsonParseException.class, e -> {
                    assertThat(e.getLine()).isEqualTo(1);
                    assertThat(e.getColumn()).isEqualTo(4);
                    assertThat(e.getMessage()).contains("trailing content");
                });
    }

    @Test
    void reportsMissingCommaBetweenMembers() {
        assertThatThrownBy(() -> JsonParser.parse("{\n  \"a\": 1\n  \"b\": 2\n}"))
                .isInstanceOfSatisfying(JsonParseException.class, e -> {
                    assertThat(e.getLine()).isEqualTo(3);
                    assertThat(e.getColumn()).isEqualTo(3);
                    assertThat(e.getMessage()).contains("Expected ',' or '}'");
                });
    }

    @Test
    void reportsEmptyInput() {
        assertThatThrownBy(() -> JsonParser.parse("   "))
                .isInstanceOfSatisfying(JsonParseException.class, e ->
                        assertThat(e.getMessage()).contains("Unexpected end of input"));
    }

    @Test
    void rejectsExcessiveNesting() {
        String input = "[".repeat(JsonParser.MAX_DEPTH + 2) + "1" + "]".repeat(JsonParser.MAX_DEPTH + 2);
        assertThatThrownBy(() -> JsonParser.parse(input))
                .isInstanceOfSatisfying(JsonParseException.class, e ->
                        assertThat(e.getMessage()).contains("Maximum nesting depth"));
    }
}
