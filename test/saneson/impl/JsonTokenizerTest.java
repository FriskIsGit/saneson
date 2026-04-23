package saneson.impl;

import static org.junit.Assert.*;
import org.junit.Test;
import saneson.core.JsonException;

import java.util.List;

public class JsonTokenizerTest {
    private static void assertTokensEqual(List<Token> expected, List<Token> actual) {
        assertEquals("token count", expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            Token expToken = expected.get(i);
            Token actToken = actual.get(i);
            assertEquals("type at index " + i, expToken.type, actToken.type);
            assertEquals("value at index " + i, expToken.value, actToken.value);
        }
    }

    @Test
    public void dontTokenizeEmptyInput() {
        var tokens = JsonTokenizer.tokenize("");
        assertTrue(tokens.isEmpty());
    }

    @Test
    public void dontTokenizeWhitespace() {
        var tokens = JsonTokenizer.tokenize("   \n\t  ");
        assertTrue(tokens.isEmpty());
    }

    @Test
    public void tokenizeBracesAndBrackets() {
        var tokens = JsonTokenizer.tokenize("{[ ]}");
        assertTokensEqual(List.of(
                new Token(Token.Type.LEFT_BRACE),
                new Token(Token.Type.LEFT_BRACKET),
                new Token(Token.Type.RIGHT_BRACKET),
                new Token(Token.Type.RIGHT_BRACE)
        ), tokens);
    }

    @Test
    public void tokenizeNormalString() {
        var tokens = JsonTokenizer.tokenize("\"two  spaced  text\"");
        assertTokensEqual(List.of(
                new Token(Token.Type.STRING, "two  spaced  text")
        ), tokens);
    }

    @Test
    public void tokenizeEscapedString() {
        var tokens = JsonTokenizer.tokenize("\"hello \\\"world\\\"\"");
        assertTokensEqual(List.of(
                new Token(Token.Type.STRING, "hello \"world\"")
        ), tokens);
    }

    @Test
    public void tokenizeNumberSequence() {
        var tokens = JsonTokenizer.tokenize("42 -3 3.14 -0.5 -0.2e+35");
        assertTokensEqual(List.of(
                new Token(Token.Type.NUMBER, "42"),
                new Token(Token.Type.NUMBER, "-3"),
                new Token(Token.Type.NUMBER, "3.14"),
                new Token(Token.Type.NUMBER, "-0.5"),
                new Token(Token.Type.NUMBER, "-0.2e+35")
        ), tokens);
    }

    @Test
    public void tokenizeTrueFalseAndNull() {
        var tokens = JsonTokenizer.tokenize("true false null");
        assertTokensEqual(List.of(
                new Token(Token.Type.TRUE),
                new Token(Token.Type.FALSE),
                new Token(Token.Type.NULL)
        ), tokens);
    }

    @Test
    public void tokenizeCommasAndColons() {
        var tokens = JsonTokenizer.tokenize("{\"a\":1, \"b\":2}");
        assertTokensEqual(List.of(
                new Token(Token.Type.LEFT_BRACE),
                new Token(Token.Type.STRING, "a"),
                new Token(Token.Type.COLON),
                new Token(Token.Type.NUMBER, "1"),
                new Token(Token.Type.COMMA),
                new Token(Token.Type.STRING, "b"),
                new Token(Token.Type.COLON),
                new Token(Token.Type.NUMBER, "2"),
                new Token(Token.Type.RIGHT_BRACE)
        ), tokens);
    }

    @Test
    public void throwOnInvalidEscape() {
        assertThrows(JsonException.class, () -> JsonTokenizer.tokenize("\"\\z\""));
    }

    @Test
    public void throwOnInvalidNumber() {
        assertThrows(JsonException.class, () -> JsonTokenizer.tokenize("-012"));
    }
}
