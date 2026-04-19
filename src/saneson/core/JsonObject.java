package saneson.core;

import java.util.ArrayList;
import java.util.List;

public class JsonObject implements JsonNode {
    List<JsonPair> level;
    public JsonObject(List<JsonPair> level) {
        this.level = level;
    }

    public List<JsonPair> pairs() {
        return level;
    }

    public JsonNode find(String key) {
        for (JsonPair pair : level) {
            if (pair.key.equals(key)) {
                return pair.node;
            }
        }
        return null;
    }

    public boolean contains(String key) {
        return find(key) != null;
    }

    public JsonObject getObjects(String ...keys) {
        JsonObject object = this;
        for (String key : keys) {
            object = object.getObject(key);
            if (object == null) {
                return null;
            }
        }
        return object;
    }

    public JsonObject getObject(String key) {
        JsonNode node = find(key);
        if (node == null) {
            return null;
        }
        if (node instanceof JsonObject obj) {
            return obj;
        }
        throw new JsonException("Field '" + key + "' is not an object");
    }

    public String getString(String key) {
        JsonValue value = getValue(key);
        if (value == null) {
            return null;
        }
        if (value.isString()) {
            return value.asString();
        }
        throw new JsonException("Field '" + key + "' is not a string");
    }

    public Double getDouble(String key) {
        JsonValue value = getValue(key);
        if (value == null) {
            return null;
        }
        if (value.isNumber()) {
            return value.asDouble();
        }
        throw new JsonException("Field '" + key + "' is not a number");
    }

    public Boolean getBoolean(String key) {
        JsonValue value = getValue(key);
        if (value == null) {
            return null;
        }
        if (value.isBoolean()) {
            return value.asBoolean();
        }
        throw new JsonException("Field '" + key + "' is not a boolean");
    }

    public List<JsonNode> getArray(String key) {
        JsonValue value = getValue(key);
        if (value == null) {
            return null;
        }
        if (value.isArray()) {
            return value.asArray();
        }
        throw new JsonException("Field '" + key + "' is not an array");
    }

    public List<Integer> getIntArray(String key) {
        List<JsonNode> array = getArray(key);
        if (array == null) {
            return null;
        }
        List<Integer> intList = new ArrayList<>(array.size());
        for (JsonNode element : array) {
            JsonValue v = element.asValue();
            if (v == null || !v.isNumber()) {
                throw new JsonException("Field '" + key + "' contains a non-number element");
            }
            intList.add(v.asDouble().intValue());
        }
        return intList;
    }

    private JsonValue getValue(String key) {
        JsonNode node = find(key);
        if (node == null) {
            return null;
        }
        if (node instanceof JsonValue val) {
            return val;
        }
        throw new JsonException("Field '" + key + "' is an object, not a value");
    }


    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public JsonValue asValue() {
        return null;
    }

    @Override
    public JsonObject asObject() {
        return this;
    }

    @Override
    public String toString() {
        return "JsonObject (pairs=" + level.size() + ")";
    }
}
