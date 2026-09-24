# gsb-q63-java-json-parser

自研 JSON 解析器与生成器，零第三方 JSON 依赖（仅测试用 JUnit 5 / AssertJ）。
面向服务间报文交换场景：错误定位精确到行列，大整数不丢精度。

## 快速开始

```bash
mvn -q verify   # 编译并运行全部测试
```

```java
import com.example.gsb.json.*;

JsonValue v = Json.parse("{\"orderId\": 9007199254740993, \"amount\": 0.1}");
long id = v.asObject().get("orderId").asNumber().longValue();   // 精确，不丢精度

String s = Json.stringify(v);          // 序列化
JsonValue v2 = Json.parse(s);          // 再解析
assert v2.equals(v);                   // 解析 → 序列化 → 再解析 结果等价
```

## 支持范围

- 全部 JSON 值类型：object、array、string、number、true、false、null
- 字符串转义：`\" \\ \/ \b \f \n \r \t` 与 `\uXXXX`（含代理对，如 emoji）
- 数字文法严格遵循 RFC 8259：`-?(0|[1-9]\d*)(\.\d+)?([eE][+-]?\d+)?`，
  前导零、缺小数位、缺指数位都会报错
- 对象保持键的插入顺序（`LinkedHashMap`）
- 程序化构造：`new JsonObject().put("k", "v")` / `new JsonArray().add(1L)`

## 数字精度：为什么用 BigDecimal 而不是 double

`double` 只有 53 位尾数，`9007199254740993`（2^53+1）会被舍入成
`9007199254740992`，`0.1` 也无法精确表示——服务间传金额、订单号时这是事故。
因此 `JsonNumber` 内部一律用 `BigDecimal` 保存词素，任意大的整数、
任意精度的小数都逐位保留。序列化用 `BigDecimal.toString()`，其输出
（含 `1E+3` 这类科学计数法）是合法 JSON 数字且能被原样读回，保证往返一致。

需要时注意：`JsonNumber.equals` 与 `BigDecimal.equals` 一致，区分标度
（`1.0` ≠ `1.00`）；纯数值比较请用 `bigDecimalValue().compareTo(...)`。
也提供 `doubleValue()` / `longValue()` 等便捷取值，但那是调用方的主动取舍。

## 错误报告

语法错误抛出 `JsonException`，携带行号、列号与可读提示：

```
line 3, column 8: invalid literal, expected 'true'
line 1, column 6: expected ':' after object key, but found number 1
line 1, column 4: unescaped control character in string, use \u000a instead
```

`JsonException#getLine()` / `getColumn()` 可程序化读取。

## 代码结构

| 类 | 职责 |
| --- | --- |
| `JsonLexer` | 词法分析：字符流 → token，记录行列号，处理转义与数字词素 |
| `JsonParser` | 语法分析：递归下降，token → 对象模型 |
| `JsonValue` 及子类 | 对象模型：`JsonObject/JsonArray/JsonString/JsonNumber/JsonBoolean/JsonNull` |
| `JsonWriter` | 序列化时的字符串转义 |
| `Json` | 门面：`parse` / `stringify` |
| `JsonException` | 带行列号的解析异常 |

## 已知限制

- 最大嵌套深度 1000 层，超出报错（防止恶意输入打爆栈）
- 对象键重复时后者覆盖前者，不报错（与多数解析器行为一致）
- 序列化为紧凑格式，不提供美化缩进输出
- 孤立的代理项（lone surrogate）不报错，原样保留
- 数字不限制量级，`1e999999` 这类值会原样保留为 `BigDecimal`
  （转成 `double` 才是 Infinity）
