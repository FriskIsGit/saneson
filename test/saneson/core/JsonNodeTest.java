package saneson.core;

import org.junit.Test;
import saneson.impl.JsonParser;

import static org.junit.Assert.*;

public class JsonNodeTest {
    private static final String STORE_JSON = "{\"store\":{\"items\":[{\"title\":\"X\"},{\"title\":\"Robots\"}]}}";
    @Test
    public void testMixedPath() {
        JsonNode root = JsonParser.parse(STORE_JSON);

        JsonNode title = root.path("store/items/1/title");

        assertNotNull(title);
        assertEquals("Robots", title.asValue().asString());
    }

    @Test
    public void testMissingPath() {
        JsonNode root = JsonParser.parse(STORE_JSON);

        assertNull(root.path("store/non-existent1/non-existent2"));
    }
}
