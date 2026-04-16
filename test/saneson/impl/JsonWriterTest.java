package saneson.impl;

import org.junit.Test;
import saneson.core.JsonNode;

import static org.junit.Assert.assertEquals;

public class JsonWriterTest {
    @Test
    public void testSimpleObjectWrite() {
        JsonNode node = JsonParser.parse("{\"name\":\"Petra\",\"year\":312,\"active\":true}");
        String output = JsonWriter.getInstance().write(node);
        String expected = """
                {
                  "name": "Petra",
                  "year": 312,
                  "active": true
                }""";
        assertEquals(expected, output);
    }
}
