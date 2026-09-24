package com.example.gsb.json;

/**
 * 语法分析器：递归下降法，把 Token 流构建为 {@link JsonValue} 对象树。
 * 所有语法错误都抛出带行号、列号与可读提示的 {@link JsonParseException}。
 */
final class JsonParser {

    /** 最大嵌套深度，防止恶意报文造成栈溢出。 */
    static final int MAX_DEPTH = 512;

    private final Lexer lexer;
    private Lexer.Token current;

    JsonParser(String source) {
        this.lexer = new Lexer(source);
        this.current = lexer.next();
    }

    /** 解析整个输入，要求根值之后只有空白。 */
    JsonValue parse() {
        JsonValue value = parseValue(0);
        if (current.type() != Lexer.TokenType.EOF) {
            throw error("根值之后存在多余内容 " + current.describe()
                    + "，一个 JSON 文档只能包含一个根值");
        }
        return value;
    }

    private JsonValue parseValue(int depth) {
        if (depth > MAX_DEPTH) {
            throw error("嵌套层级过深（超过 " + MAX_DEPTH + " 层），可能存在异常报文");
        }
        return switch (current.type()) {
            case LBRACE -> parseObject(depth);
            case LBRACKET -> parseArray(depth);
            case STRING -> {
                JsonString s = new JsonString(current.text());
                advance();
                yield s;
            }
            case NUMBER -> {
                JsonNumber n = new JsonNumber(current.numberValue());
                advance();
                yield n;
            }
            case TRUE -> { advance(); yield JsonBoolean.TRUE; }
            case FALSE -> { advance(); yield JsonBoolean.FALSE; }
            case NULL -> { advance(); yield JsonNull.INSTANCE; }
            default -> throw error("此处应为一个 JSON 值（对象/数组/字符串/数字/true/false/null），"
                    + "但遇到 " + current.describe());
        };
    }

    private JsonObject parseObject(int depth) {
        advance(); // 跳过 {
        JsonObject obj = new JsonObject();
        if (current.type() == Lexer.TokenType.RBRACE) {
            advance();
            return obj;
        }
        while (true) {
            if (current.type() != Lexer.TokenType.STRING) {
                throw error("对象的键必须是字符串，但遇到 " + current.describe()
                        + "（注意：JSON 不允许尾随逗号）");
            }
            String key = current.text();
            advance();
            expect(Lexer.TokenType.COLON, "键 \"" + abbreviate(key) + "\" 之后应为 ':'");
            obj.put(key, parseValue(depth + 1));
            if (current.type() == Lexer.TokenType.COMMA) {
                advance();
                continue;
            }
            if (current.type() == Lexer.TokenType.RBRACE) {
                advance();
                return obj;
            }
            throw error("对象成员之间应为 ','，或以 '}' 结束，但遇到 " + current.describe());
        }
    }

    private JsonArray parseArray(int depth) {
        advance(); // 跳过 [
        JsonArray arr = new JsonArray();
        if (current.type() == Lexer.TokenType.RBRACKET) {
            advance();
            return arr;
        }
        while (true) {
            arr.add(parseValue(depth + 1));
            if (current.type() == Lexer.TokenType.COMMA) {
                advance();
                if (current.type() == Lexer.TokenType.RBRACKET) {
                    throw error("数组元素之后应为新的元素，但遇到 ']'（JSON 不允许尾随逗号）");
                }
                continue;
            }
            if (current.type() == Lexer.TokenType.RBRACKET) {
                advance();
                return arr;
            }
            throw error("数组元素之间应为 ','，或以 ']' 结束，但遇到 " + current.describe());
        }
    }

    private void expect(Lexer.TokenType type, String hint) {
        if (current.type() != type) {
            throw error(hint + "，但遇到 " + current.describe());
        }
        advance();
    }

    private void advance() {
        current = lexer.next();
    }

    private JsonParseException error(String detail) {
        return new JsonParseException(current.line(), current.column(), detail);
    }

    private static String abbreviate(String s) {
        return s.length() <= 20 ? s : s.substring(0, 20) + "...";
    }
}
