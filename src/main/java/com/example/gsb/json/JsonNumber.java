package com.example.gsb.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * JSON 数字。内部使用 {@link BigDecimal} 保存，保证任意大小的整数与
 * 任意精度的小数都不丢精度（不会像 double 那样把 9007199254740993
 * 舍入成 9007199254740992）。
 */
public final class JsonNumber implements JsonValue {

    private final BigDecimal value;

    public JsonNumber(BigDecimal value) {
        this.value = Objects.requireNonNull(value, "数字值不能为 null");
    }

    public static JsonNumber of(long value) {
        return new JsonNumber(BigDecimal.valueOf(value));
    }

    public static JsonNumber of(BigInteger value) {
        return new JsonNumber(new BigDecimal(value));
    }

    /** 完整精度。 */
    public BigDecimal bigDecimalValue() {
        return value;
    }

    /** 可能丢精度的便捷转换，调用方需自行确认值在 long 范围内。 */
    public long longValue() {
        return value.longValue();
    }

    /** 可能丢精度的便捷转换。 */
    public double doubleValue() {
        return value.doubleValue();
    }

    @Override
    public JsonNumber asNumber() {
        return this;
    }

    @Override
    public String toJson() {
        // BigDecimal.toString() 的输出（含科学计数法形式如 1E+2）本身即合法 JSON 数字，
        // 且能被 BigDecimal 再次无差别解析，保证往返一致。
        return value.toString();
    }

    @Override
    public String toPrettyJson() {
        return toJson();
    }

    @Override
    public boolean equals(Object o) {
        // 注意：BigDecimal.equals 区分精度标度（1.0 与 1.00 不相等）。
        // 这正符合往返一致的要求：序列化会保留标度，再解析后完全相等。
        return o instanceof JsonNumber other && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return toJson();
    }
}
