# gsb-q63-java-json-parser

自研 JSON 解析器与生成器，不依赖 Jackson / Gson / Fastjson 等任何现成 JSON 库。
词法分析（Lexer）与语法分析（递归下降 Parser）均为手写实现，仅使用 JDK 标准类。

## 构建与测试

```bash
mvn -q verify
```

## 快速上手

```java
import com.example.gsb.json.*;

JsonObject obj = JsonParser.parseObject("{\"id\": 9007199254740993, \"name\": \"gsb\"}");
BigDecimal id = obj.get("id").asNumber();   // 精度完整保留
String name = obj.get("name").asString();

String json = obj.toJson();                  // 序列化为紧凑 JSON
JsonValue again = JsonParser.parse(json);    // 再解析，与 obj 相等
```

## 支持范围

- **值类型**：对象、数组、字符串、数字、`true` / `false` / `null`，支持任意嵌套。
- **字符串**：支持 `\"` `\\` `\/` `\b` `\f` `\n` `\r` `\t` 转义，以及 `\uXXXX`
  Unicode 转义（含 UTF-16 代理对，如 `😀`）；拒绝未转义的控制字符（U+0000–U+001F）。
- **数字**：完整 JSON 数字文法（可选负号、整数部分、小数部分、指数部分），
  拒绝前导零（`01`）、缺少数字的 `1.` / `1e` 等非法形式。
- **错误报告**：所有语法错误抛出 `JsonParseException`，携带 1 起始的行号、列号与
  可读提示，例如：
  `Unexpected character '@' (line 2, column 8)`。
- **对象模型**：`JsonObject`（保持成员插入顺序）、`JsonArray`、`JsonString`、
  `JsonNumber`、`JsonBoolean`、`JsonNull`，均不可变或受控可变，提供 `equals/hashCode`。
- **序列化**：`toJson()` 输出符合 RFC 8259 的紧凑 JSON，保证
  「解析 → 序列化 → 再解析」得到的值树相等（`JsonNumber` 按数值比较，`1` 与 `1.0` 相等）。

## 数字为何用 BigDecimal 而不是 double

`double` 只有 53 位有效二进制位（约 15–17 位十进制有效数字）：

- 大于 2^53 的整数无法精确表示，例如 `9007199254740993`（2^53+1）会被舍入为
  `9007199254740992`，服务间传递长整型 ID 时直接出错；
- 高精度小数（如金额 `99999999999999999999999999.00000001`）同样会丢失尾数。

因此 `JsonNumber` 内部使用 `java.math.BigDecimal` 保存词法单元解析出的精确值，
序列化时用 `toPlainString()` 输出全部有效数字，任意精度整数与小数都能无损往返。
代价是解析与运算比 `double` 慢，但对「报文交换不能错」的场景这是正确取舍。
如需浮点近似值，调用方可以自行 `asNumber().doubleValue()`。

## 代码结构

| 文件 | 职责 |
| --- | --- |
| `Lexer.java` | 词法分析：字符流 → `Token` 流，维护行号/列号 |
| `Token.java` | 词法单元（类型 + 文本 + 位置） |
| `JsonParser.java` | 递归下降语法分析：`Token` 流 → `JsonValue` 树 |
| `JsonValue` 及子类 | 对象模型（Object/Array/String/Number/Boolean/Null） |
| `JsonWriter.java` | 序列化：值树 → 紧凑 JSON 文本 |
| `JsonParseException.java` | 带行号列号的解析异常 |

## 已知限制

- **非流式**：整段输入一次性读入内存并构建完整值树，不适合 GB 级超大报文。
- **嵌套深度上限 512 层**：递归下降解析用 JVM 栈实现，超限抛出
  `JsonParseException` 以防栈溢出（`JsonParser.MAX_DEPTH`）。
- **数字序列化格式归一化**：`1e3` 序列化为 `1000`，`1.0` 序列化为 `1.0`；
  数值相等（`compareTo == 0`）即视为相等，不保留原始词形。
- **对象键重复时后者覆盖前者**（与多数 JSON 库行为一致），不报错。
- **序列化仅紧凑格式**，暂不提供美化缩进输出；非 ASCII 字符按原样输出（合法 JSON），
  不强制转义为 `\uXXXX`。
