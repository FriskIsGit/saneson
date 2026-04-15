package saneson.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class JsonValue implements JsonNode {
    Object value;

    public JsonValue(Object value) {
        this.value = value;
    }

    public String asString() {
        return (value instanceof String s) ? s : null;
    }

    public Double asDouble() {
        return (value instanceof JsonNumber n) ? n.asDouble() : null;
    }

    public Integer asInt() {
        return (value instanceof JsonNumber n) ? n.asInt() : null;
    }

    public Long asLong() {
        return (value instanceof JsonNumber n) ? n.asLong() : null;
    }

    public BigDecimal asBigDecimal() {
        return (value instanceof JsonNumber n) ? n.asBigDecimal() : null;
    }

    public BigInteger asBigInteger() {
        return (value instanceof JsonNumber n) ? n.asBigInteger() : null;
    }

    public JsonNumber asNumber() {
        return (value instanceof JsonNumber n) ? n : null;
    }

    public Boolean asBoolean() {
        return (value instanceof Boolean b) ? b : null;
    }

    @SuppressWarnings("unchecked")
    public List<JsonNode> asArray() {
        return (value instanceof List<?> list) ? (List<JsonNode>) list : null;
    }

    public boolean isArray() {
        return value instanceof List<?>;
    }

    public boolean isString() {
        return value instanceof String;
    }

    public boolean isNumber() {
        return value instanceof JsonNumber;
    }

    public boolean isBoolean() {
        return value instanceof Boolean;
    }

    public boolean isNull() {
        return value == null;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public JsonValue asValue() {
        return this;
    }

    @Override
    public JsonObject asObject() {
        return null;
    }

    @Override
    public String toString() {
        return asString();
    }
}
