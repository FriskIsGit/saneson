package saneson.impl;

import saneson.core.*;

import java.util.*;

public final class JsonParser {
    private final List<Token> tokens;
    private int pos = 0;

    private JsonParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public static JsonNode parse(String json) {
        List<Token> tokens = JsonTokenizer.tokenize(json);
        return parse(tokens);
    }

    static JsonNode parse(List<Token> tokens) {
        JsonParser parser = new JsonParser(tokens);
        JsonNode value = parser.parseValue(0);
        if (parser.hasToken()) {
            throw new JsonException("Extra token after JSON value: " + parser.token());
        }
        return value;
    }

    private boolean hasToken() {
        return pos < tokens.size();
    }

    private Token token() {
        return tokens.get(pos);
    }

    private void next() {
        pos++;
    }

    private void expect(Token.Type type) {
        if (!hasToken() || token().type != type) {
            throw new JsonException("Expected " + type + " but got " + (hasToken() ? token() : "EOF"));
        }
    }

    private JsonNode parseValue(int depth) {
        if (!hasToken()) {
            throw new JsonException("Unexpected EOF");
        }
        Token t = token();
        switch (t.type) {
            case LEFT_BRACE:
                return parseObject(depth);
            case LEFT_BRACKET:
                return parseArray(depth);
            case STRING:
                next();
                return new JsonValue(t.value);
            case NUMBER:
                next();
                return new JsonValue(Double.valueOf(t.value));
            case TRUE:
                next();
                return new JsonValue(Boolean.TRUE);
            case FALSE:
                next();
                return new JsonValue(Boolean.FALSE);
            case NULL:
                next();
                return new JsonValue(null);
            default:
                throw new JsonException("Unexpected token: " + t);
        }
    }

    private JsonObject parseObject(int depth) {
        next();
        List<JsonPair> level = new ArrayList<>();
        if (hasToken() && token().type == Token.Type.RIGHT_BRACE) {
            next();
            return new JsonObject(level);
        }
        while (true) {
            Token keyToken = token();
            if (keyToken.type != Token.Type.STRING)
                throw new JsonException("Object keys must be STRING but got " + keyToken);
            next();
            expect(Token.Type.COLON);
            next();
            JsonNode value = parseValue(depth + 1);
            var jsonPair = new JsonPair(keyToken.value, value);
            level.add(jsonPair);
            if (!hasToken()) {
                throw new JsonException("Unterminated object");
            }
            switch (token().type) {
                case COMMA -> next();
                case RIGHT_BRACE -> {
                    next();
                    return new JsonObject(level);
                }
                default -> throw new JsonException("Expected ',' or '}' in object but got " + token());
            }
        }
    }

    private JsonValue parseArray(int depth) {
        next();
        List<JsonNode> arr = new ArrayList<>();
        // Check empty array before parsing value
        if (hasToken() && token().type == Token.Type.RIGHT_BRACKET) {
            next();
            return new JsonValue(arr);
        }
        while (true) {
            JsonNode value = parseValue(depth);
            arr.add(value);
            if (!hasToken()) {
                throw new JsonException("Unterminated array");
            }
            switch (token().type) {
                case COMMA -> next();
                case RIGHT_BRACKET -> { next(); return new JsonValue(arr); }
                default -> throw new JsonException("Expected ',' or ']' in array but got " + token());
            }
        }
    }
}

