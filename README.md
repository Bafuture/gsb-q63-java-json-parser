# 自研 JSON 解析器与生成器

不依赖 Jackson / Gson / Fastjson 等任何现成 JSON 库，从零实现词法分析、
语法分析、对象模型与序列化，面向服务间报文交换场景。

## 快速开始

```bash
./mvnw -q verify   # 编译并运行全部测试（40 个用例）
```

```java
import com.example.gsb.json.*;

// 解析
JsonValue value = Json.parse("{\"orderId\": 9007199254740993, \"tags\": [\"a\", \"b\"]}");
JsonObject obj = value.asObject();
BigDecimal id = obj.get("orderId").asNumber().bigDecimalValue(); // 精度完整

// 序列化
String compact = Json.stringify(value);          // {"orderId":9007199254740993,"tags":["a","b"]}
String pretty  = Json.stringify(value, true);    // 2 空格缩进的美化输出

// 手工构建
JsonObject doc = new JsonObject()
        .put("name", "订单")
        .put("paid", true)
        .put("note", JsonNull.INSTANCE)
        .put("items", new JsonArray().add("x").add(JsonNumber.of(42)));
```

## 支持范围

- **六种 JSON 值**：对象、数组、字符串、数字、布尔（`true`/`false`）、`null`。
- **词法分析**（`Lexer`）：把字符流切分为带行号、列号的 Token；
  严格按 JSON 数字文法扫描（拒绝前导零 `01`、裸小数点 `1.`、空指数 `1e` 等）。
- **语法分析**（`JsonParser`）：递归下降法，把 Token 流构建为对象树。
- **字符串转义**：`\" \\ \/ \b \f \n \r \t` 与 `\uXXXX`；
  支持 UTF-16 代理项对（如 `\uD83D\uDE00` → 😀），孤立代理项会报错。
- **对象模型**：`JsonValue` sealed 接口 + 六个实现类
  （`JsonObject`/`JsonArray`/`JsonString`/`JsonNumber`/`JsonBoolean`/`JsonNull`），
  对象键序按插入顺序保留。
- **序列化**：紧凑与美化两种输出；控制字符按规范转义；
  保证「解析 → 序列化 → 再解析」结果等价（测试覆盖，含二次序列化定点）。

## 数字为什么用 BigDecimal 而不是 double

`double` 只有 53 位有效二进制位，超过 2^53 的整数会被舍入
（`9007199254740993` 变成 `9007199254740992`），小数也会丢失尾数精度。
服务间报文常见订单号、金额、雪花 ID 等大整数/高精度小数，丢精度不可接受。

因此 `JsonNumber` 内部用 **`BigDecimal`** 保存词法分析阶段截取的原始数字文本：

- 任意长度的整数、任意精度的小数都原样保留；
- `BigDecimal.toString()` 的输出本身即合法 JSON 数字，且能被再次等价解析，
  保证往返一致（连 `1.0` 与 `1.00` 的标度差异都不丢失）；
- 同时提供 `longValue()`/`doubleValue()` 便捷转换（可能丢精度，由调用方自行判断）。

代价是解析与运算比 `double` 慢，但对报文交换场景，正确性优先于极限性能。

## 错误定位

所有语法错误抛出 `JsonParseException`，携带 1 起始的行号、列号与可读提示：

```java
try {
    Json.parse("{\n  \"a\": 1,\n  \"b\": tru\n}");
} catch (JsonParseException e) {
    e.getLine();    // 3
    e.getColumn();  // 8
    e.getMessage(); // 第 3 行第 8 列：无法识别的字面量，是否想写 'true'？
}
```

典型提示还包括：缺少冒号/逗号、尾随逗号、字符串未结束、非法转义、
未转义的控制字符、前导零、根值后有多余内容、嵌套过深等。

## 已知限制

- **非流式**：整个输入一次性读入内存，不适合超大报文（GB 级）。
- **嵌套深度上限 512 层**：防止恶意报文造成栈溢出，超限报
  `JsonParseException`（`JsonParser.MAX_DEPTH` 可调）。
- **数字范围**：指数部分受 `BigDecimal` 限制（约 ±21 亿数量级），
  极端如 `1e9999999999` 会抛 `NumberFormatException` 而非 `JsonParseException`。
- **非 ASCII 字符原样输出**：序列化不做 `\uXXXX` 全转义（JSON 默认 UTF-8，
  合法且更紧凑）；如需纯 ASCII 输出需自行后处理。
- **只接受严格 JSON**：不支持注释、单引号、尾随逗号、`NaN`/`Infinity`
  等 JSON5/JS 扩展。
- **`JsonNumber.equals` 区分标度**：`1.0` 与 `1.00` 不相等
  （与 `BigDecimal.equals` 一致），比较数值请用
  `bigDecimalValue().compareTo(...)`。

## 代码结构

```
src/main/java/com/example/gsb/json/
├── Json.java                门面：parse / stringify
├── Lexer.java               词法分析（Token + 行列号）
├── JsonParser.java          语法分析（递归下降）
├── JsonWriter.java          序列化（紧凑/美化、字符串转义）
├── JsonParseException.java  带行列号的语法错误异常
└── JsonValue.java + 6 个实现类   对象模型（sealed 类型层级）

src/test/java/com/example/gsb/json/
├── JsonParserTest.java      正常解析
├── JsonErrorTest.java       错误定位
└── JsonRoundTripTest.java   往返一致
```
