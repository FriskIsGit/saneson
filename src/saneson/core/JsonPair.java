package saneson.core;

public class JsonPair {
    String key;
    JsonNode element;

    public JsonPair(String key, JsonNode element) {
        this.key = key;
        this.element = element;
    }
}
