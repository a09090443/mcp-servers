# Date MCP 服務器

此 MCP 服務器提供日期和時區操作功能，允許您獲取當前日期、處理不同時區的日期時間轉換，以及進行其他日期相關操作。

## 功能列表

### 日期操作
- **獲取當前日期**：獲取今天的日期，支援自定義格式
- **檢查日期**：判斷指定日期是否為今天

### 時區操作
- **獲取時區日期時間**：獲取指定時區的當前日期時間
- **時區日期轉換**：在不同時區之間轉換日期時間
- **獲取可用時區**：列出系統支援的所有時區
- **按地區查詢時區**：獲取指定地區（如亞洲、歐洲、美洲）的所有時區

## 技術需求

### 開發環境
- 語言：Kotlin
- JDK 版本：21
- 構建工具：Gradle
- 框架：Quarkus 3.x

### 主要依賴庫
- Quarkus Kotlin
- Java Time API：用於日期和時區操作
- Gson：用於 JSON 格式化
- MCP Server STDIO：用於 MCP 服務通信

## 設定與配置

### 構建與運行
使用 Gradle 構建並運行服務：

```
./gradlew build
java -jar build/quarkus-app/quarkus-run.jar
```

構建 uber-jar：

```
./gradlew build -Dquarkus.package.jar.type=uber-jar
```

開發模式運行：

```
./gradlew quarkusDev
```

### MCP 配置文件設定
您可以在 MCP 配置文件中添加日期服務：

```json
{
  "mcpServers": {
    "date": {
      "command": "java",
      "args": [
        "-jar", "path/to/date-1.0-SNAPSHOT-runner.jar"
      ]
    }
  }
}
```

配置說明：
- 此設定將日期 MCP 服務命名為 date
- command 指定要執行的命令
- args 包含執行命令的參數，指向日期 MCP 服務的 JAR 文件
- 請根據您的實際環境調整 JAR 文件的路徑

### 與 Langchain4j 整合範例
以下是與 Langchain4j 整合的範例程式碼：

```kotlin
val dateTransport = StdioMcpTransport.Builder()
    .command(listOf("java", "-jar", "date-1.0-SNAPSHOT-runner.jar"))
    .logEvents(true)
    .build()
val dateClient = DefaultMcpClient.Builder().transport(dateTransport).build()
val toolProvider = McpToolProvider.builder().mcpClients(listOf(dateClient)).build()
aiService.toolProvider(toolProvider)
```

### 日誌配置
日誌文件配置在 application.properties 中：

```
quarkus.package.type=uber-jar
quarkus.log.file.enable=true
quarkus.log.file.path=D:/tmp/date.log
```
