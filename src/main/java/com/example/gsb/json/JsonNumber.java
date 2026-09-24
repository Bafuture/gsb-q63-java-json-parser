package com.example.gsb.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * JSON 数字，内部用 {@link BigDecimal} 保存，不丢精度：
 * 大整数（如 9007199254740993）和高精度小数（如 0.1）都能原样保留。
 * equals/hashCode 与 BigDecimal 一致（区分 1.0 与 1.00 的标度），
 * 数值比较请用 {@link #bigDecimalValue()} 的 compareTo。
 */
public final class JsonNumber extends JsonValue {

    private final BigDecimal value;

    public JsonNumber(BigDecimal value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public BigDecimal bigDecimalValue() {
        return value;
    }

    public BigInteger bigIntegerValue() {
        return value.toBigInteger();
    }

    public long longValue() {
        return value.longValue();
    }

    public int intValue() {
        return value.intValue();
    }

    public double doubleValue() {
        return value.doubleValue();
    }

    @Override
    public JsonNumber asNumber() {
        return this;
    }

    @Override
    void writeJson(StringBuilder sb) {
        // BigDecimal.toString 的结果（含科学计数法如 1E+3）是合法 JSON 数字，
        // 且能被 BigDecimal 构造器原样读回，保证往返一致。
        sb.append(value.toString());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof JsonNumber other && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
