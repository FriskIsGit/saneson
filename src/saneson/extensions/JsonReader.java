package saneson.extensions;

import saneson.core.JsonException;
import saneson.core.JsonNode;
import saneson.core.JsonObject;
import saneson.core.JsonValue;
import saneson.impl.JsonParser;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JsonReader {
    private static final HashMap<Class<?>, CachedClass> cachedClasses = new HashMap<>();
    private static final boolean IGNORE_TRANSIENT = true;

    public static <T> T read(String json, Class<T> clazz) throws JsonException {
        if (json == null) {
            return null;
        }
        return read(JsonParser.parse(json), clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T read(JsonNode node, Class<T> clazz) throws JsonException {
        if (node == null) {
            return null;
        }
        return (T) read(node, (Type) clazz);
    }

    private static Object read(JsonNode node, Type type) throws JsonException {
        if (node == null) {
            return null;
        }
        if (node.isObject()) {
            return readObject(node.asObject(), rawClass(type));
        }
        return readValue(node.asValue(), type);
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

            Object fieldValue = read(node, field.getGenericType());
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

    private static List<Object> readList(List<JsonNode> elements, Type genericType) throws JsonException {
        if (elements == null) {
            return null;
        }
        Type typeT;
        if (genericType instanceof ParameterizedType listType) {
            typeT = listType.getActualTypeArguments()[0];
        } else {
            typeT = Object.class;
        }

        List<Object> out = new ArrayList<>(elements.size());
        for (JsonNode element : elements) {
            Object object = read(element, typeT);
            out.add(object);
        }
        return out;
    }

    private static Object readArray(List<JsonNode> elements, Type componentType) throws JsonException {
        Object array = Array.newInstance(rawClass(componentType), elements.size());
        for (int i = 0; i < elements.size(); i++) {
            Array.set(array, i, read(elements.get(i), componentType));
        }
        return array;
    }

    private static Object readValue(JsonValue value, Type type) throws JsonException {
        if (value.isNull()) {
            return null;
        }
        Class<?> raw = rawClass(type);
        if (value.isArray()) {
            List<JsonNode> elements = value.asArray();
            if (List.class.isAssignableFrom(raw)) {
                return readList(elements, type);
            }
            // Array with a non-reifiable component type: T[], List<String>[]
            if (type instanceof GenericArrayType gat) {
                return readArray(elements, gat.getGenericComponentType());
            }
            // Array with a concrete component type: String[], int[], Object[]
            if (raw.isArray()) {
                return readArray(elements, raw.getComponentType());
            }
            throw new JsonException("Cannot map JSON array to type " + raw.getName() + " (expected List or array)");
        }
        return value.as(raw);
    }

    private static Class<?> rawClass(Type type) {
        if (type instanceof Class<?> c) {
            return c;
        }
        if (type instanceof ParameterizedType pt) {
            return (Class<?>) pt.getRawType();
        }
        if (type instanceof GenericArrayType gat) {
            return rawClass(gat.getGenericComponentType()).arrayType();
        }
        return Object.class;
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
