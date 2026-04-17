package saneson.impl;

import saneson.core.JsonException;
import saneson.core.JsonNode;
import saneson.extensions.Json;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class JsonReader {
    public static <T> T read(String json, Class<T> clazz) throws JsonException {
        Constructor<T> constructor;
        try {
            constructor = clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new JsonException(e.getMessage());
        }
        T instance;
        try {
            instance = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new JsonException(e.getMessage());
        }

        JsonNode node = JsonParser.parse(json);
        // TODO: Handle primitive types

        for (Field field : clazz.getDeclaredFields()) {
            Json jsonAnnotation = field.getAnnotation(Json.class);
            if (jsonAnnotation == null || jsonAnnotation.key().isEmpty()) {
                continue;
            }

            boolean canAccess = field.canAccess(instance);
            if (!canAccess) {
                field.setAccessible(true);
            }

            String key = jsonAnnotation.key();
            // TODO: Read node into instance
        }
        return instance;
    }
}
