package saneson.core;

public class JsonPair {
    String key;
    JsonElement element;

    public JsonPair(String key, JsonElement element) {
        this.key = key;
        this.element = element;
    }
}
