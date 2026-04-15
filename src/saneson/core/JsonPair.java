package saneson.core;

public class JsonPair {
    String key;
    JsonNode node;

    public JsonPair(String key, JsonNode node) {
        this.key = key;
        this.node = node;
    }

    public String key() {
        return key;
    }

    public JsonNode node() {
        return node;
    }
}
