package saneson.core;

import org.junit.Test;
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
        List<Token> tokens = JsonTokenizer.tokenize("{}");

        Object result = JsonParser.parse(tokens);

        assertNotNull(result);
    }
}

