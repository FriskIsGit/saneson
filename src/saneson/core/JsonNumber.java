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

    @Override
    public String toString() {
        return raw;
    }
}
