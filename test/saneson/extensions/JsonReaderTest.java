package saneson.extensions;

import org.junit.Test;
import saneson.core.JsonException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

public class JsonReaderTest {
    private static final double DELTA = 0.0001f;

    @Test
    public void deserializeIntoFloatPrimitive() {
        float result = JsonReader.read("8.22", float.class);

        assertEquals(8.22, result, DELTA);
    }

    @Test
    public void deserializeIntoString() {
        String result = JsonReader.read("\"lol\"", String.class);

        assertEquals("lol", result);
    }

    @Test
    public void deserializeIntoSimpleObject() {
        Dummy expected = new Dummy("John", 87);
        Dummy result = JsonReader.read("{\"name\": \"John\", \"age\": 87}", Dummy.class);

        assertTrue(expected.equals(result));
    }

    @Test
    public void deserializeIntoEnum() {
        Arrow result = JsonReader.read("\"UP\"", Arrow.class);

        assertEquals(Arrow.UP, result);
    }

    @Test
    public void deserializeIntoObjectWithList() {
        Bag expected = new Bag("tools", List.of("hammer", "nail", "screw"));
        Bag result = JsonReader.read(
                "{\"label\": \"tools\", \"items\": [\"hammer\", \"nail\", \"screw\"]}",
                Bag.class);

        assertTrue(expected.equals(result));
    }

    @Test
    public void handleGenericList() {
        ListWrapper expected = new ListWrapper(List.of("hammer", "nail", "screw"));
        ListWrapper result = JsonReader.read("{\"items\": [\"hammer\", \"nail\", \"screw\"]}", ListWrapper.class);

        assertTrue(expected.equals(result));
    }

    @Test
    public void handleListOfClassObjects() {
        ClassObjects expected = new ClassObjects(List.of(new Dummy("hammer", 1), new Dummy("nail", 2)));
        ClassObjects result = JsonReader.read(
                "{\"dummies\": [{\"name\": \"hammer\", \"age\": 1}, {\"name\": \"nail\", \"age\": 2}]}",
                ClassObjects.class);

        assertTrue(expected.equals(result));
    }

    @Test
    public void handleListOfStringArrays() {
        ListOfArrays expected = new ListOfArrays(List.of(
                new String[]{"a", "b"},
                new String[]{"c", "d"}));
        ListOfArrays result = JsonReader.read(
                "{\"rows\": [[\"a\", \"b\"], [\"c\", \"d\"]]}",
                ListOfArrays.class);

        assertTrue(expected.equals(result));
    }

    @Test
    public void handleArrayOfLists() {
        ArrayOfLists expected = new ArrayOfLists(new List[]{
                List.of("a", "b"),
                List.of("c", "d")});
        ArrayOfLists result = JsonReader.read(
                "{\"rows\": [[\"a\", \"b\"], [\"c\", \"d\"]]}",
                ArrayOfLists.class);

        assertTrue(expected.equals(result));
    }

    @Test
    public void handleNestedLists() {
        NestedLists expected = new NestedLists(List.of(List.of("a", "b"), List.of("c", "d")));
        NestedLists result = JsonReader.read(
                "{\"grid\": [[\"a\", \"b\"], [\"c\", \"d\"]]}",
                NestedLists.class);

        assertTrue(expected.equals(result));
    }

    @Test
    public void readUsingJsonAnnotationKey() {
        Aliased expected = new Aliased("John Doe", null);
        Aliased result = JsonReader.read("{\"full_name\": \"John Doe\", \"fullName\": \"ignored\"}", Aliased.class);

        assertTrue(expected.equals(result));
    }

    @Test
    public void skipIgnoredField() {
        Vault expected = new Vault(null, "keep");
        Vault result = JsonReader.read("{\"secret\": \"dropped\", \"kept\": \"keep\"}", Vault.class);

        assertTrue(expected.equals(result));
    }

    @Test
    public void skipsNullValueForPrimitiveField() {
        Dummy result = JsonReader.read("{\"name\": \"dummy\", \"age\": null}", Dummy.class);

        assertEquals("dummy", result.name);
        assertEquals(0, result.age);
    }

    @Test
    public void handleRawList() {
        List<String> expected = List.of("hammer", "nail", "screw");
        List<String> result = JsonReader.read("[\"hammer\", \"nail\", \"screw\"]", ArrayList.class);

        assertTrue(expected.equals(result));
    }

    @Test
    public void handleNullInPrimitiveArray() {
        IntHolder result = JsonReader.read("{\"numbers\": [1, null, 3]}", IntHolder.class);

        assertArrayEquals(new int[]{1, 0, 3}, result.numbers);
    }

    @Test
    public void readEmptyObjectIntoStringGivesClearError() {
        assertThrows(JsonException.class, () -> JsonReader.read("{}", String.class));
    }
}

class IntHolder {
    int[] numbers;

    IntHolder() {
    }
}

class Dummy {
    String name;
    int age;

    private Dummy() {
    }

    public Dummy(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public boolean equals(Dummy dummy) {
        return age == dummy.age && name.equals(dummy.name);
    }
}

enum Arrow {
    UP, DOWN, LEFT, RIGHT
}

class Bag {
    String label;
    List<String> items;

    Bag() {
    }

    public Bag(String label, List<String> items) {
        this.label = label;
        this.items = items;
    }

    public boolean equals(Bag bag) {
        return label.equals(bag.label) && items.equals(bag.items);
    }
}

@SuppressWarnings({"rawtypes"})
class ListWrapper {
    List items;

    ListWrapper() {
    }

    public ListWrapper(List items) {
        this.items = items;
    }

    public boolean equals(ListWrapper wrapper) {
        return items.equals(wrapper.items);
    }
}

class ClassObjects {
    List<Dummy> dummies;

    ClassObjects() {
    }

    public ClassObjects(List<Dummy> dummies) {
        this.dummies = dummies;
    }

    public boolean equals(ClassObjects classObjects) {
        if (dummies.size() != classObjects.dummies.size()) {
            return false;
        }
        for (int i = 0; i < dummies.size(); i++) {
            if (!dummies.get(i).equals(classObjects.dummies.get(i))) {
                return false;
            }
        }
        return true;
    }
}

class ListOfArrays {
    List<String[]> rows;

    ListOfArrays() {
    }

    public ListOfArrays(List<String[]> rows) {
        this.rows = rows;
    }

    public boolean equals(ListOfArrays other) {
        if (rows == null && other == null) {
            return true;
        }
        if (rows == null || other.rows == null) {
            return false;
        }
        if (rows.size() != other.rows.size()) {
            return false;
        }
        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);
            String[] otherRow = other.rows.get(i);
            if (row.length != otherRow.length) {
                return false;
            }
            for (int j = 0; j < row.length; j++) {
                if (!row[j].equals(otherRow[j])) {
                    return false;
                }
            }
        }
        return true;
    }
}

class ArrayOfLists {
    List<String>[] rows;

    ArrayOfLists() {
    }

    public ArrayOfLists(List<String>[] rows) {
        this.rows = rows;
    }

    public boolean equals(ArrayOfLists other) {
        if (rows.length != other.rows.length) {
            return false;
        }
        for (int i = 0; i < rows.length; i++) {
            if (!rows[i].equals(other.rows[i])) {
                return false;
            }
        }
        return true;
    }
}

class Vault {
    @Json(ignored = true)
    String secret;
    String kept;

    Vault() {
    }

    public Vault(String secret, String kept) {
        this.secret = secret;
        this.kept = kept;
    }

    public boolean equals(Vault other) {
        return Objects.equals(secret, other.secret) && Objects.equals(kept, other.kept);
    }
}

class Aliased {
    @Json("full_name")
    String fullName;
    String otherName;

    Aliased() {
    }

    public Aliased(String fullName, String otherName) {
        this.fullName = fullName;
        this.otherName = otherName;
    }

    public boolean equals(Aliased other) {
        return Objects.equals(fullName, other.fullName) && Objects.equals(otherName, other.otherName);
    }
}

class NestedLists {
    List<List<String>> grid;

    NestedLists() {
    }

    public NestedLists(List<List<String>> grid) {
        this.grid = grid;
    }

    public boolean equals(NestedLists other) {
        if (grid.size() != other.grid.size()) {
            return false;
        }
        for (int i = 0; i < grid.size(); i++) {
            List<String> row = grid.get(i);
            List<String> otherRow = other.grid.get(i);
            if (row.size() != otherRow.size()) {
                return false;
            }
            for (int j = 0; j < row.size(); j++) {
                if (!row.get(j).equals(otherRow.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }
}