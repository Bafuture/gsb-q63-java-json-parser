package com.example.gsb.json;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * JSON number node backed by {@link BigDecimal} so that integers larger than
 * {@code long} and high-precision decimals keep every significant digit.
 */
public final class JsonNumber extends JsonValue {

    private final BigDecimal value;

    public JsonNumber(BigDecimal value) {
        this.value = Objects.requireNonNull(value, "number value must not be null");
    }

    public JsonNumber(long value) {
        this(BigDecimal.valueOf(value));
    }

    public JsonNumber(double value) {
        this(BigDecimal.valueOf(value));
    }

    public static JsonNumber of(String lexeme) {
        return new JsonNumber(new BigDecimal(lexeme));
    }

    public BigDecimal value() {
        return value;
    }

    @Override
    public BigDecimal asNumber() {
        return value;
    }

    @Override
    String typeName() {
        return "number";
    }

    @Override
    public String toJson() {
        // toPlainString keeps the full precision and never emits scientific
        // notation; the result is always a valid JSON number.
        return value.toPlainString();
    }

    @Override
    public boolean equals(Object other) {
        // Numeric equality: 1 and 1.0 are the same JSON number value.
        return other instanceof JsonNumber n && value.compareTo(n.value) == 0;
    }

    @Override
    public int hashCode() {
        // stripTrailingZeros so equal numeric values hash the same.
        return value.stripTrailingZeros().hashCode();
    }
}
