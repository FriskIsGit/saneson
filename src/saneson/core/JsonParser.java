package saneson.core;

import java.util.*;

public final class JsonParser {
    private final List<Token> tokens;
    private int pos = 0;

    private JsonParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    static Object parse(List<Token> tokens) {
        JsonParser parser = new JsonParser(tokens);
        Object value = parser.parseValue(0);
        if (parser.hasNext()) {
            throw new JsonException("Extra token after JSON value: " + parser.token());
        }
        return value;
    }

    private boolean hasNext() {
        return pos + 1 < tokens.size();
    }

    private Token token() {
        return tokens.get(pos);
    }

    private Token next() {
        return tokens.get(pos++);
    }

    private void expect(Token.Type type) {
        if (!hasNext() || token().type != type) {
            throw new JsonException("Expected " + type + " but got " + (hasNext() ? token() : "EOF"));
        }
    }

    private Object parseValue(int depth) {
        if (!hasNext()) {
            throw new JsonException("Unexpected EOF");
        }
        Token t = token();
        return switch (t.type) {
            case LEFT_BRACE -> parseObject(depth);
            case LEFT_BRACKET -> parseArray(depth);
            case STRING -> {
                next();
                yield t.value;
            }
            case NUMBER -> {
                next();
                yield Double.valueOf(t.value);
            }
            case TRUE -> {
                next();
                yield Boolean.TRUE;
            }
            case FALSE -> {
                next();
                yield Boolean.FALSE;
            }
            case NULL -> {
                next();
                yield null;
            }
            default -> throw new JsonException("Unexpected token: " + t);
        };
    }

    private List<JsonPair> parseObject(int depth) {
        List<JsonPair> level = new ArrayList<>();
        if (hasNext() && token().type == Token.Type.RIGHT_BRACE) {
            next();
            return level;
        }
        while (true) {
            Token keyToken = token();
            if (keyToken.type != Token.Type.STRING)
                throw new JsonException("Object keys must be STRING but got " + keyToken);
            next();
            String key = keyToken.value;
            expect(Token.Type.COLON);
            Object value = parseValue(depth + 1);
            // parseValue may need to return type information
            var jsonPair = new JsonPair(key, value);
            level.add(jsonPair);
            if (!hasNext()) {
                throw new JsonException("Unterminated object");
            }
            if (token().type == Token.Type.COMMA) {
                next();
            } else if (token().type == Token.Type.RIGHT_BRACE) {
                next();
                break;
            } else {
                throw new JsonException("Expected ',' or '}' in object but got " + token());
            }
        }
        return level;
    }

    private List<Object> parseArray(int depth) {
        List<Object> arr = new ArrayList<>();
        // Check empty array before parsing value
        if (hasNext() && token().type == Token.Type.RIGHT_BRACKET) {
            next();
            return arr;
        }
        while (true) {
            Object value = parseValue(depth);
            arr.add(value);
            if (!hasNext()) {
                throw new JsonException("Unterminated array");
            }
            if (token().type == Token.Type.COMMA) {
                next();
            } else if (token().type == Token.Type.RIGHT_BRACKET) {
                next();
                break;
            } else {
                throw new JsonException("Expected ',' or ']' in array but got " + token());
            }
        }
        return arr;
    }
}



