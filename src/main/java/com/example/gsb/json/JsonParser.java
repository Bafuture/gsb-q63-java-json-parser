package com.example.gsb.json;

import java.math.BigDecimal;
import java.util.List;

/**
 * 语法分析器：递归下降，把 token 序列组装成对象模型。
 * 数字一律用 {@link BigDecimal} 解析，任何精度都不丢失。
 */
public final class JsonParser {

    /** 最大嵌套深度，防止恶意输入导致栈溢出。 */
    private static final int MAX_DEPTH = 1000;

    private final List<JsonLexer.Token> tokens;
    private int index;

    private JsonParser(String input) {
        this.tokens = new JsonLexer(input).tokenize();
    }

    public static JsonValue parse(String input) {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null");
        }
        JsonParser parser = new JsonParser(input);
        JsonValue value = parser.parseValue(0);
        JsonLexer.Token extra = parser.peek();
        if (extra.type() != JsonLexer.TokenType.EOF) {
            throw parser.error(extra, "unexpected " + extra + " after top-level value");
        }
        return value;
    }

    private JsonValue parseValue(int depth) {
        if (depth > MAX_DEPTH) {
            JsonLexer.Token t = peek();
            throw error(t, "nesting too deep (max " + MAX_DEPTH + ")");
        }
        JsonLexer.Token t = peek();
        return switch (t.type()) {
            case LBRACE -> parseObject(depth);
            case LBRACKET -> parseArray(depth);
            case STRING -> { advance(); yield new JsonString(t.text()); }
            case NUMBER -> { advance(); yield new JsonNumber(new BigDecimal(t.text())); }
            case TRUE -> { advance(); yield JsonBoolean.TRUE; }
            case FALSE -> { advance(); yield JsonBoolean.FALSE; }
            case NULL -> { advance(); yield JsonNull.INSTANCE; }
            default -> throw error(t, "expected a value (object, array, string, number, "
                    + "true, false or null), but found " + t);
        };
    }

    private JsonObject parseObject(int depth) {
        advance(); // '{'
        JsonObject object = new JsonObject();
        if (peek().type() == JsonLexer.TokenType.RBRACE) {
            advance();
            return object;
        }
        while (true) {
            JsonLexer.Token key = peek();
            if (key.type() != JsonLexer.TokenType.STRING) {
                throw error(key, "expected a string as object key, but found " + key);
            }
            advance();
            expect(JsonLexer.TokenType.COLON, "expected ':' after object key");
            object.put(key.text(), parseValue(depth + 1));
            JsonLexer.Token next = peek();
            if (next.type() == JsonLexer.TokenType.COMMA) {
                advance();
            } else if (next.type() == JsonLexer.TokenType.RBRACE) {
                advance();
                return object;
            } else {
                throw error(next, "expected ',' or '}' in object, but found " + next);
            }
        }
    }

    private JsonArray parseArray(int depth) {
        advance(); // '['
        JsonArray array = new JsonArray();
        if (peek().type() == JsonLexer.TokenType.RBRACKET) {
            advance();
            return array;
        }
        while (true) {
            array.add(parseValue(depth + 1));
            JsonLexer.Token next = peek();
            if (next.type() == JsonLexer.TokenType.COMMA) {
                advance();
            } else if (next.type() == JsonLexer.TokenType.RBRACKET) {
                advance();
                return array;
            } else {
                throw error(next, "expected ',' or ']' in array, but found " + next);
            }
        }
    }

    private void expect(JsonLexer.TokenType type, String message) {
        JsonLexer.Token t = peek();
        if (t.type() != type) {
            throw error(t, message + ", but found " + t);
        }
        advance();
    }

    private JsonLexer.Token peek() {
        return tokens.get(index);
    }

    private void advance() {
        if (index < tokens.size() - 1) {
            index++;
        }
    }

    private JsonException error(JsonLexer.Token token, String message) {
        return new JsonException(token.line(), token.column(), message);
    }
}
