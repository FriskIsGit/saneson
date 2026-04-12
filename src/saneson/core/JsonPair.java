package saneson.core;

enum JsonValueType {
    OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL
}

public class JsonPair {
    String key;
    JsonValueType type;
    Object value;

    public JsonPair(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    boolean isObject() {
        return type == JsonValueType.OBJECT;
    }

    boolean isArray() {
        return type == JsonValueType.ARRAY;
    }

    boolean isString() {
        return type == JsonValueType.STRING;
    }

    boolean isNumber() {
        return type == JsonValueType.NUMBER;
    }

    boolean isBoolean() {
        return type == JsonValueType.BOOLEAN;
    }

    boolean isNull() {
        return type == JsonValueType.NULL;
    }

    JsonValueType getType() {
        return type;
    }
}
