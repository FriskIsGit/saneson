# saneson

A small, dependency-free JSON library for Java 17+

Saneson parses JSON into a simple tree, lets you navigate it with JSON Pointer (RFC 6901), and write it back out. 
It is deliberately minimal.

## Quick start

```java
import saneson.core.JsonNode;
import saneson.core.JsonObject;
import saneson.impl.JsonParser;
import saneson.impl.JsonWriter;

JsonNode root = JsonParser.parse("{\"name\":\"Petra\",\"year\":312,\"active\":true}");

JsonObject obj = root.asObject();
String name    = obj.getString("name");     // "Petra"
Double year    = obj.getDouble("year");     // 312.0
Boolean active = obj.getBoolean("active");  // true

// JSON Pointer navigation
JsonNode title = root.path("/store/book/0/title");

// Pretty-print back to a string
String prettyJson = JsonWriter.getInstance().write(root);
System.out.println(prettyJson);
```

## The `JsonNode` tree

`JsonNode` is the interface every node in the parsed tree implements. It has exactly two shapes:

```
JsonNode
├── JsonObject   — a JSON object: ordered list of key/value pairs
└── JsonValue    — everything else: string, number, boolean, null, array
```

Arrays are a `JsonValue` whose underlying Java value is a `List<JsonNode>`. 
Keeping the hierarchy simple means every traversal boils down to a single `instanceof JsonObject` check.

### `JsonObject`

Stores an ordered `List<JsonPair>`, so the fields are not shuffled. It exposes typed getters that throw `JsonException` on type mismatch and return `null` for missing keys:

```java
obj.getString("name");
obj.getDouble("year");
obj.getBoolean("active");
obj.getObject("address");
obj.getObjects("a", "b", "c");   // walk nested objects safely
obj.getArray("tags");
obj.getIntArray("ids");
obj.contains("key");
obj.pairs();                     // raw access for iteration
```

### `JsonValue`

Internally wraps an `Object` which is one of: `String`, `JsonNumber`, `Boolean`, `List<JsonNode>`, or `null`. 
The `is…()` / `as…()` methods dispatch on the type:

| Check         | Convert                               |
|---------------|---------------------------------------|
| `isString()`  | `asString()`                          |
| `isNumber()`  | `asNumber()`, `asDouble()`, `asInt()` |
| `isBoolean()` | `asBoolean()`                         |
| `isArray()`   | `asArray()` → `List<JsonNode>`        |
| `isNull()`    |                                       |

### `JsonNumber` — why numbers are different

Every other primitive maps cleanly onto a Java type but numbers don't have that luxury. 
JSON's number syntax has *arbitrary precision* — there is no upper bound on digits or distinction between integer and float.
In Java parsing `3954342058245098345943850394852409809582049` into a `double` truncates it to the nearest representable value.
`JsonNumber` overcomes this by storing the raw numeric literal as a `String` and deferring conversion until the caller asks for a specific Java type:

```java
public record JsonNumber(String raw) {
    public double     asDouble()     { ... }
    public int        asInt()        { ... }
    public long       asLong()       { ... }
    public BigDecimal asBigDecimal() { ... }
    public BigInteger asBigInteger() { ... }
}
```
## JSON Pointer

`JsonNode.path(String)` implements RFC 6901. Returns `null` when any segment fails to resolve (missing key, out-of-bounds index, wrong node type).

```java
root.path("");                    // root
root.path("/users/0/email");      // first user's email
root.path("/does/not/exist");     // null
```

## Writing JSON

`JsonWriter` pretty-prints with two-space indentation.
## Layout

```
src/saneson/
├── core/        public API — JsonNode, JsonObject, JsonValue, JsonNumber, JsonPair, JsonException
├── impl/        parser, tokenizer, writer
└── extensions/  experimental annotation stubs
```
