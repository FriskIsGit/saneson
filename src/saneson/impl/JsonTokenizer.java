package saneson.impl;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Character.isDigit;

public class JsonTokenizer {
    static List<Token> tokenize(String rawJson) {
        List<Token> tokens = new ArrayList<>();
        int len = rawJson.length();
        int i = 0;

        while (i < len) {
            char c = rawJson.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            switch (c) {
                case '{' -> { tokens.add(new Token(Token.Type.LEFT_BRACE)); i++; }
                case '}' -> { tokens.add(new Token(Token.Type.RIGHT_BRACE)); i++; }
                case '[' -> { tokens.add(new Token(Token.Type.LEFT_BRACKET)); i++; }
                case ']' -> { tokens.add(new Token(Token.Type.RIGHT_BRACKET)); i++; }
                case ':' -> { tokens.add(new Token(Token.Type.COLON)); i++; }
                case ',' -> { tokens.add(new Token(Token.Type.COMMA)); i++; }
                case '"' -> {
                    var result = tokenizeString(rawJson, i);
                    tokens.add(result.token);
                    i = result.index;
                }
                default -> {
                    Token token;
                    if (c == '-' || isDigit(c)) {
                        var result = tokenizeNumber(rawJson, i);
                        token = result.token;
                        i = result.index;
                    } else if (rawJson.startsWith("true", i)) {
                        token = new Token(Token.Type.TRUE);
                        i += 4;
                    } else if (rawJson.startsWith("false", i)) {
                        token = new Token(Token.Type.FALSE);
                        i += 5;
                    } else if (rawJson.startsWith("null", i)) {
                        token = new Token(Token.Type.NULL);
                        i += 4;
                    } else {
                        throw new IllegalArgumentException("Unexpected character at " + i + ": " + c);
                    }
                    tokens.add(token);
                }
            }
        }
        return tokens;
    }

    /**
     * Tokenizes a JSON string
     *
     * @param rawJson entire JSON input
     * @param from index where the string is opened
     * @return {@link TokenResult} result
    **/
    static TokenResult tokenizeString(String rawJson, int from) {
        int len = rawJson.length();
        StringBuilder str = new StringBuilder();
        int i = from + 1;
        while (i < len) {
            char ch = rawJson.charAt(i);
            if (ch == '"') {
                i++;
                break;
            }
            i++;
            // If not an escape sequence: append and go next
            if (ch != '\\') {
                str.append(ch);
                continue;
            }
            if (i >= len) {
                throw new IllegalArgumentException("Unterminated escape");
            }
            char escapeChar = rawJson.charAt(i++);
            switch (escapeChar) {
                case '"' -> str.append('"');
                case '\\' -> str.append('\\');
                case '/' -> str.append('/');
                case 'b' -> str.append('\b');
                case 'f' -> str.append('\f');
                case 'n' -> str.append('\n');
                case 'r' -> str.append('\r');
                case 't' -> str.append('\t');
                case 'u' -> {
                    if (i + 4 > len) {
                        throw new IllegalArgumentException("EOS parsing unicode escape");
                    }

                    String hex = rawJson.substring(i, i + 4);
                    str.append((char) Integer.parseInt(hex, 16));
                    i += 4;
                }
                default -> throw new IllegalArgumentException("Invalid escape: \\" + escapeChar);
            }
        }
        var token = new Token(Token.Type.STRING, str.toString());
        return new TokenResult(token, i);
    }

    /**
     * Tokenizes a JSON number
     *
     * @param json entire JSON input
     * @param from index where the number starts
     * @return {@link TokenResult} result
     **/
    static TokenResult tokenizeNumber(String json, int from) {
        int len = json.length();
        int i = from;
        if (json.charAt(i) == '-') {
            i++;
            ensureDigit(json, i, '-');
        }
        if (json.charAt(i) == '0') {
            int next = i + 1;
            if (next < len && isDigit(json.charAt(next))) {
                throw new IllegalArgumentException("Zero cannot be followed by a digit");
            }
        }
        i = skipUntilNonDigit(json, i+1);

        if (i < len && json.charAt(i) == '.') {
            ensureDigit(json, i+1, '.');
            i = skipUntilNonDigit(json, i+2);
        }
        if (i < len && (json.charAt(i) == 'e' || json.charAt(i) == 'E')) {
            i++;
            if (i < len && (json.charAt(i) == '+' || json.charAt(i) == '-')) {
                i++;
            }
            ensureDigit(json, i, 'e');
            i = skipUntilNonDigit(json, i+1);
        }
        String num = json.substring(from, i);
        var token = new Token(Token.Type.NUMBER, num);
        return new TokenResult(token, i);
    }

    /**
     * Return value structure
     *
     * @param token token
     * @param index exclusive index where the string/number is closed/ends
     **/
    record TokenResult(Token token, int index){}

    private static void ensureDigit(String json, int at, char after) {
        int len = json.length();
        if (at >= len) {
            throw new IllegalArgumentException("EOS parsing number, expected digit.");
        }
        if (!isDigit(json.charAt(at))) {
            throw new IllegalArgumentException("Expected digit after '" + after + "'");
        }
    }

    private static int skipUntilNonDigit(String str, int i) {
        int len = str.length();
        while (i < len && isDigit(str.charAt(i))) {
            i++;
        }
        return i;
    }

    public static void main(String[] args) {
        String sample = "{\"name\":\"John\",\"age\":33,\"active\":false,\"tags\":[\"x\",\"y\"]}";
        for (Token token : tokenize(sample)) {
            System.out.println(token);
        }
    }
}



class Token {
    enum Type {
        LEFT_BRACE, RIGHT_BRACE, LEFT_BRACKET, RIGHT_BRACKET,
        COLON, COMMA, STRING, NUMBER, TRUE, FALSE, NULL
    }

    public final Type type;
    public final String value; // only for STRING/NUMBER

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Token(Type type) {
        this.type = type;
        this.value = null;
    }

    public String toString() {
        return value == null ? type.name() : type + "(" + value + ")";
    }
}
