package saneson.core;

import java.util.List;

public interface JsonNode {
    JsonValue asValue();
    JsonObject asObject();

    /**
     * Shorthand for {@code instanceof JsonObject}; provides no performance benefit.
     **/
    boolean isObject();

    /**
     * Resolves a JSON Pointer path against the current JSON structure and returns
     * the referenced element.
     * <p>
     * JsonNode node = root.path("/store/book/0/title");
     * </p>
     * @param path JSON Pointer string (see RFC 6901), e.g. "/a/b/0"
     **/
    default JsonNode path(String path) {
        JsonNode element = this;
        int start = 0;
        while (start <= path.length()) {
            int slash = path.indexOf('/', start);
            int end = slash == -1 ? path.length() : slash;
            String segment = path.substring(start, end);

            if (element instanceof JsonObject obj) {
                element = obj.find(segment);
            } else {
                JsonValue v = element.asValue();
                if (v == null || !v.isArray()) {
                    return null;
                }
                List<JsonNode> arr = v.asArray();
                int i;
                try {
                    i = Integer.parseInt(segment);
                } catch (NumberFormatException e) {
                    return null;
                }
                if (i < 0 || i >= arr.size()) {
                    return null;
                }
                element = arr.get(i);
            }
            if (element == null) {
                return null;
            }
            if (slash == -1) {
                break;
            }
            start = slash + 1;
        }
        return element;
    }
}
