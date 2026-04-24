package saneson.impl;

import saneson.core.JsonException;
import saneson.core.JsonNode;
import saneson.core.JsonObject;
import saneson.core.JsonPair;
import saneson.core.JsonValue;

import java.util.List;

public class JsonWriter {
    private static final String INDENT = "  ";
    private static final int DEFAULT_MAX_DEPTH = 64;
    private static final JsonWriter writer = new JsonWriter();

    /**
     * Writes the provided JsonNode using the default JsonWriter instance.
     */
    public static String writeDefault(JsonNode node) {
        return writer.write(node);
    }

    public String write(JsonNode node) {
        StringBuilder json = new StringBuilder();
        writeNode(json, node, 0);
        return json.toString();
    }

    private void writeNode(StringBuilder json, JsonNode node, int depth) {
        if (depth > DEFAULT_MAX_DEPTH) {
            throw new JsonException("Nesting depth exceeded maximum of " + DEFAULT_MAX_DEPTH);
        }
        if (node instanceof JsonObject obj) {
            writeObject(json, obj, depth);
        } else {
            writeValue(json, (JsonValue) node, depth);
        }
    }

    private void writeObject(StringBuilder json, JsonObject obj, int depth) {
        List<JsonPair> pairs = obj.pairs();
        if (pairs.isEmpty()) {
            json.append("{}");
            return;
        }
        json.append('{').append('\n');
        for (int i = 0; i < pairs.size(); i++) {
            indent(json, depth + 1);
            JsonPair pair = pairs.get(i);
            writeString(json, pair.key());
            json.append(": ");
            writeNode(json, pair.node(), depth + 1);
            if (i < pairs.size() - 1) {
                json.append(',');
            }
            json.append('\n');
        }
        indent(json, depth);
        json.append('}');
    }

    private void writeValue(StringBuilder json, JsonValue value, int depth) {
        if (value.isNull()) {
            json.append("null");
        } else if (value.isString()) {
            writeString(json, value.asString());
        } else if (value.isBoolean()) {
            json.append(value.asBoolean() ? "true" : "false");
        } else if (value.isNumber()) {
            json.append(value.asNumber().raw());
        } else if (value.isArray()) {
            writeArray(json, value.asArray(), depth);
        }
    }

    private void writeArray(StringBuilder json, List<JsonNode> arr, int depth) {
        if (arr.isEmpty()) {
            json.append("[]");
            return;
        }
        json.append('[').append('\n');
        for (int i = 0; i < arr.size(); i++) {
            indent(json, depth + 1);
            writeNode(json, arr.get(i), depth + 1);
            if (i < arr.size() - 1) {
                json.append(',');
            }
            json.append('\n');
        }
        indent(json, depth);
        json.append(']');
    }

    private void writeString(StringBuilder json, String s) {
        json.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> json.append("\\\"");
                case '\\' -> json.append("\\\\");
                case '\b' -> json.append("\\b");
                case '\f' -> json.append("\\f");
                case '\n' -> json.append("\\n");
                case '\r' -> json.append("\\r");
                case '\t' -> json.append("\\t");
                default -> {
                    if (c < 0x20) {
                        json.append(String.format("\\u%04x", (int) c));
                    } else {
                        json.append(c);
                    }
                }
            }
        }
        json.append('"');
    }

    private void indent(StringBuilder json, int depth) {
        json.append(INDENT.repeat(depth));
    }
}
