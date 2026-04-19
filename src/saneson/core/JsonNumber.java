package saneson.core;

import java.math.BigDecimal;
import java.math.BigInteger;

public record JsonNumber(String raw) {

    public double asDouble() {
        return Double.parseDouble(raw);
    }

    public int asInt() {
        return Integer.parseInt(raw);
    }

    public long asLong() {
        return Long.parseLong(raw);
    }

    public BigDecimal asBigDecimal() {
        return new BigDecimal(raw);
    }

    public BigInteger asBigInteger() {
        return asBigDecimal().toBigIntegerExact();
    }

    @SuppressWarnings("unchecked")
    public <T> T as(Class<T> clazz) {
        Object result;
        if (clazz == Double.class || clazz == double.class) {
            result = asDouble();
        } else if (clazz == Integer.class || clazz == int.class) {
            result = asInt();
        } else if (clazz == Long.class || clazz == long.class) {
            result = asLong();
        } else if (clazz == Float.class || clazz == float.class) {
            result = (float) asDouble();
        } else if (clazz == Short.class || clazz == short.class) {
            result = (short) asInt();
        } else if (clazz == Byte.class || clazz == byte.class) {
            result = (byte) asInt();
        } else if (clazz == BigDecimal.class) {
            result = asBigDecimal();
        } else if (clazz == BigInteger.class) {
            result = asBigInteger();
        } else return null;

        if (clazz.isPrimitive()) {
            return (T) result;
        } else {
            return clazz.cast(result);
        }
    }

    @Override
    public String toString() {
        return raw;
    }
}
