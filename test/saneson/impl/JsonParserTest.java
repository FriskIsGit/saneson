package saneson.impl;

import org.junit.Test;
import saneson.core.JsonNode;
import saneson.core.JsonException;
import saneson.core.JsonObject;

import static org.junit.Assert.*;

public class JsonParserTest {
    @Test
    public void throwsOnEmptyJson() {
        assertThrows(JsonException.class, () -> JsonParser.parse("   "));
    }

    @Test
    public void parsesEmptyObject() {
        JsonNode result = JsonParser.parse("{}");

        assertNotNull(result);
        assertTrue(result.isObject());
    }

    @Test
    public void parseJsonToString() {
        JsonNode result = JsonParser.parse("\"hello\"");

        assertNotNull(result);
        assertFalse(result.isObject());
        assertEquals("hello", result.asValue().toString());
    }

    @Test
    public void throwsWhenMaxDepthIsExceeded() {
        String json = "{\"a\":{\"b\":{\"c\":{}}}}";
        assertThrows(JsonException.class, () -> JsonParser.parse(json, 2));
    }

    @Test
    public void throwsWhenMaxDepthIsExceededWithNestedArray() {
        String json = "{\"a\":[{\"b\":[{\"c\":1}]}]}";
        assertThrows(JsonException.class, () -> JsonParser.parse(json, 2));
    }

    @Test
    public void parsesSimpleObject() {
        String json = "{\"name\":\"Petra\",\"year\":312,\"active\":true}";

        JsonNode result = JsonParser.parse(json);

        assertTrue(result.isObject());
        JsonObject obj = result.asObject();
        assertEquals("Petra", obj.getString("name"));
        assertEquals(Double.valueOf(312), obj.getDouble("year"));
        assertEquals(true, obj.getBoolean("active"));
    }

    @Test
    public void throwOnUnterminatedObject() {
        assertThrows(JsonException.class, () -> JsonParser.parse("{"));
    }

    @Test
    public void throwsOnTrailingCommaInObject() {
        assertThrows(JsonException.class, () -> JsonParser.parse("{\"a\":1,"));
    }

    @Test
    public void throwsOnUnterminatedString() {
        assertThrows(JsonException.class, () -> JsonParser.parse("\"abc"));
    }

    @Test
    public void throwsOnInvalidUnicodeEscape() {
        JsonException ex = assertThrows(JsonException.class, () -> JsonParser.parse("\"\\uZZZZ\""));
        assertTrue("Expected message to mention unicode escape, got: " + ex.getMessage(),
                ex.getMessage().toLowerCase().contains("unicode"));
    }
}

