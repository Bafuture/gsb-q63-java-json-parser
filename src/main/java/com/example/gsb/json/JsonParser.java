package com.example.gsb.json;

import java.util.Objects;

/**
 * Recursive-descent parser building the {@link JsonValue} tree from tokens
 * produced by {@link Lexer}. Syntax errors are reported as
 * {@link JsonParseException} with a 1-based line/column position.
 */
public final class JsonParser {

    /** Maximum nesting depth, guarding against stack overflow on hostile input. */
    static final int MAX_DEPTH = 512;

    private final Lexer lexer;
    private Token current;

    private JsonParser(String input) {
        this.lexer = new Lexer(input);
        this.current = lexer.next();
    }

    /** Parses the given text into a {@link JsonValue} tree. */
    public static JsonValue parse(String input) {
        Objects.requireNonNull(input, "input must not be null");
        JsonParser parser = new JsonParser(input);
        JsonValue value = parser.parseValue(0);
        parser.expect(Token.Type.EOF, "Unexpected trailing content after the JSON value");
        return value;
    }

    public static JsonObject parseObject(String input) {
        JsonValue value = parse(input);
        if (!(value instanceof JsonObject object)) {
            throw new IllegalArgumentException("Top-level JSON value is not an object but " + value.typeName());
        }
        return object;
    }

    public static JsonArray parseArray(String input) {
        JsonValue value = parse(input);
        if (!(value instanceof JsonArray array)) {
            throw new IllegalArgumentException("Top-level JSON value is not an array but " + value.typeName());
        }
        return array;
    }

    private JsonValue parseValue(int depth) {
        if (depth > MAX_DEPTH) {
            throw error("Maximum nesting depth of " + MAX_DEPTH + " exceeded", current);
        }
        return switch (current.type) {
            case LEFT_BRACE -> parseObject(depth);
            case LEFT_BRACKET -> parseArray(depth);
            case STRING -> {
                JsonString value = new JsonString(current.text);
                advance();
                yield value;
            }
            case NUMBER -> {
                JsonNumber value = JsonNumber.of(current.text);
                advance();
                yield value;
            }
            case TRUE -> { advance(); yield JsonBoolean.TRUE; }
            case FALSE -> { advance(); yield JsonBoolean.FALSE; }
            case NULL -> { advance(); yield JsonNull.INSTANCE; }
            case EOF -> throw error("Unexpected end of input, expected a JSON value", current);
            default -> throw error("Unexpected token " + describe(current) + ", expected a JSON value", current);
        };
    }

    private JsonObject parseObject(int depth) {
        advance(); // consume '{'
        JsonObject object = new JsonObject();
        if (current.type == Token.Type.RIGHT_BRACE) {
            advance();
            return object;
        }
        while (true) {
            if (current.type != Token.Type.STRING) {
                throw error("Expected a string as object key but found " + describe(current), current);
            }
            String key = current.text;
            advance();
            expect(Token.Type.COLON, "Expected ':' after object key \"" + key + '"');
            object.set(key, parseValue(depth + 1));
            if (current.type == Token.Type.COMMA) {
                advance();
                if (current.type == Token.Type.RIGHT_BRACE) {
                    throw error("Trailing comma in object is not allowed", current);
                }
            } else if (current.type == Token.Type.RIGHT_BRACE) {
                advance();
                return object;
            } else {
                throw error("Expected ',' or '}' in object but found " + describe(current), current);
            }
        }
    }

    private JsonArray parseArray(int depth) {
        advance(); // consume '['
        JsonArray array = new JsonArray();
        if (current.type == Token.Type.RIGHT_BRACKET) {
            advance();
            return array;
        }
        while (true) {
            array.add(parseValue(depth + 1));
            if (current.type == Token.Type.COMMA) {
                advance();
                if (current.type == Token.Type.RIGHT_BRACKET) {
                    throw error("Trailing comma in array is not allowed", current);
                }
            } else if (current.type == Token.Type.RIGHT_BRACKET) {
                advance();
                return array;
            } else {
                throw error("Expected ',' or ']' in array but found " + describe(current), current);
            }
        }
    }

    private void expect(Token.Type type, String message) {
        if (current.type != type) {
            throw error(message + ", found " + describe(current), current);
        }
        advance();
    }

    private void advance() {
        current = lexer.next();
    }

    private static JsonParseException error(String message, Token token) {
        return new JsonParseException(message, token.line, token.column);
    }

    private static String describe(Token token) {
        return switch (token.type) {
            case EOF -> "end of input";
            case STRING -> "string \"" + token.text + '"';
            case NUMBER -> "number " + token.text;
            case TRUE -> "'true'";
            case FALSE -> "'false'";
            case NULL -> "'null'";
            case LEFT_BRACE -> "'{'";
            case RIGHT_BRACE -> "'}'";
            case LEFT_BRACKET -> "'['";
            case RIGHT_BRACKET -> "']'";
            case COMMA -> "','";
            case COLON -> "':'";
        };
    }
}
