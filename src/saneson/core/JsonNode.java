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
     * the referenced node.
     * <p>
     * JsonNode node = root.path("/store/book/0/title");
     * </p>
     * @param path JSON Pointer string (see RFC 6901), e.g. "/a/b/0"
     **/
    default JsonNode path(String path) {
        if (path.isEmpty()) {
            return this;
        }
        // Non-empty pointers must start with '/' otherwise they're invalid
        if (!path.startsWith("/")) {
            return null;
        }
        JsonNode node = this;
        int start = 1;
        while (start <= path.length()) {
            int slash = path.indexOf('/', start);
            int end = slash == -1 ? path.length() : slash;
            String segment = path.substring(start, end);

            if (node instanceof JsonObject obj) {
                node = obj.find(segment);
            } else {
                JsonValue v = node.asValue();
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
                node = arr.get(i);
            }
            if (node == null) {
                return null;
            }
            if (slash == -1) {
                break;
            }
            start = slash + 1;
        }
        return node;
    }
}
