package saneson.extensions;

import saneson.core.JsonException;
import saneson.core.JsonNode;
import saneson.core.JsonObject;
import saneson.core.JsonValue;
import saneson.impl.JsonParser;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonReader {
    private static final HashMap<Class<?>, CachedClass> cachedClasses = new HashMap<>();
    private static final boolean IGNORE_TRANSIENT = true;

    public static <T> T read(String json, Class<T> clazz) throws JsonException {
        if (json == null) {
            return null;
        }
        return read(JsonParser.parse(json), clazz);
    }

    public static <T> T read(JsonNode node, Class<T> clazz) throws JsonException {
        if (node == null) {
            return null;
        }
        if (node.isObject()) {
            return readObject(node.asObject(), clazz);
        } else {
            return readValue(node.asValue(), clazz);
        }
    }

    public static <T> T readObject(JsonObject object, Class<T> clazz) throws JsonException {
        Constructor<T> constructor = getConstructor(clazz);
        T instance;
        try {
            instance = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new JsonException(e.getMessage());
        }

        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) || (IGNORE_TRANSIENT && Modifier.isTransient(mod))) {
                continue;
            }
            if (!field.trySetAccessible()) {
                throw new JsonException("Unable to access field: " + field.getName() + " of type " + field.getType());
            }

            String key = field.getName();
            Json annotation = field.getAnnotation(Json.class);
            if (annotation != null && !annotation.key().isEmpty()) {
                key = annotation.key();
            }

            JsonNode node = object.find(key);
            if (node == null) {
                continue;
            }

            Class<?> type = field.getType();

            Object fieldValue;
            if (node.isObject()) {
                fieldValue = readObject(node.asObject(), type);
                setFieldValue(instance, field, fieldValue);
                continue;
            }

            JsonValue jsonValue = node.asValue();
            if (jsonValue.isNull()) {
                setFieldValue(instance, field, null);
                continue;
            }
            var listField = List.class.isAssignableFrom(type);
            var arrayField = type.isArray();
            var isFieldIterable = listField || arrayField;

            if (jsonValue.isArray() && !isFieldIterable) {
                throw new JsonException("Cannot map JSON array to field '" + field.getName() +
                        "' of type " + type.getName() + " (expected List or array)");
            }

            if (listField) {
                fieldValue = readList(jsonValue.asArray(), field.getGenericType());
            } else if (arrayField) {
                fieldValue = readArray(jsonValue.asArray(), type.getComponentType());
            } else {
                fieldValue = readValue(jsonValue, type);
            }

            setFieldValue(instance, field, fieldValue);
        }
        return instance;
    }

    private static void setFieldValue(Object instance, Field field, Object fieldValue) throws JsonException {
        try {
            field.set(instance, fieldValue);
        } catch (IllegalAccessException e) {
            throw new JsonException("Failed to set field '" + field.getName() + "': " + e.getMessage());
        }
    }

    // genericType in this case encapsulates List<T> providing both
    private static List<Object> readList(List<JsonNode> elements, Type genericType) {
        if (elements == null) {
            return null;
        }
        Type typeT;
        if (genericType instanceof ParameterizedType listType) {
            typeT = listType.getActualTypeArguments()[0];
        } else {
            // genericType is not a ParameterizedType if the List is generic (no <T>)
            typeT = Object.class;
        }

        List<Object> out = new ArrayList<>(elements.size());
        for (JsonNode element : elements) {
            Object elementValue;
            if (typeT instanceof ParameterizedType nestedTypeT) {
                if (element.isObject()) {
                    throw new JsonException("Expected JSON array for nested List but got object");
                }
                elementValue = readList(element.asValue().asArray(), nestedTypeT);
            } else if (typeT instanceof Class<?> c) {
                elementValue = read(element, c);
            } else {
                elementValue = read(element, Object.class);
            }
            out.add(elementValue);
        }
        return out;
    }

    private static Object readArray(List<JsonNode> elements, Class<?> componentType) {
        Object array = Array.newInstance(componentType, elements.size());
        for (int i = 0; i < elements.size(); i++) {
            Array.set(array, i, read(elements.get(i), componentType));
        }
        return array;
    }

    public static <T> T readValue(JsonValue value, Class<T> clazz) throws JsonException {
        if (value.isNull()) {
            return null;
        }
        return value.as(clazz);
    }

    private static <T> Constructor<T> getConstructor(Class<T> clazz) throws JsonException {
        Constructor<T> constructor;
        try {
            constructor = clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new JsonException("Cannot deserialize " + clazz.getName() + ": no-arg constructor is missing.");
        }
        if (!constructor.trySetAccessible()) {
            String moduleName = clazz.getModule().getName() != null ? clazz.getModule().getName() : "";
            throw new JsonException(
                    "Module '" + moduleName + "' does not open '" +
                            clazz.getPackageName() + "' for reflection. " +
                            "Use --add-opens or declare 'opens' in module-info.java.");
        }
        return constructor;
    }
}

record CachedClass(Constructor<?> constructor, Field[] fields) {}