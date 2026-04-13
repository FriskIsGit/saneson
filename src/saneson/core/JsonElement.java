package saneson.core;

public interface JsonElement {
    boolean isObject();
    JsonValue asValue();
    JsonObject asObject();
}
