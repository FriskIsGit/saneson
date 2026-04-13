package saneson.impl;

import org.junit.Test;
import saneson.core.JsonElement;
import saneson.core.JsonException;
import saneson.core.JsonObject;

import static org.junit.Assert.*;
import java.util.*;

public class JsonParserTest {
    @Test
    public void throwsOnEmptyJson() {
        assertThrows(JsonException.class, () -> {
            List<Token> tokens = JsonTokenizer.tokenize("   ");
            JsonParser.parse(tokens);
        });
    }

    @Test
    public void parsesEmptyObject() {
        JsonElement result = JsonParser.parse("{}");

        assertNotNull(result);
        assertTrue(result.isObject());
    }

    @Test
    public void parseJsonToString() {
        JsonElement result = JsonParser.parse("\"hello\"");

        assertNotNull(result);
        assertFalse(result.isObject());
        assertEquals("hello", result.asValue().toString());
    }

    @Test
    public void parsesSimpleObject() {
        String json = "{\"name\":\"Petra\",\"year\":312,\"active\":true}";

        JsonElement result = JsonParser.parse(json);

        assertTrue(result.isObject());
        JsonObject obj = result.asObject();
        assertEquals("Petra", obj.getString("name"));
        assertEquals(Double.valueOf(312), obj.getDouble("year"));
        assertEquals(true, obj.getBoolean("active"));
    }
}

