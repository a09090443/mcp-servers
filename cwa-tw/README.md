# 氣象資訊 MCP 服務器

此 MCP 服務器提供台灣地區氣象資料的存取功能，包括城市天氣預報和地震觀測資料。服務基於台灣中央氣象署的開放資料 API 實現。

## 功能列表

### 天氣預報功能
- **獲取城市 API ID**: 獲取對應台灣各城市/縣的 API ID
- **獲取鄉鎮天氣預報**: 獲取台灣各鄉鎮詳細的天氣預報資料
    - 支持指定時間範圍查詢
    - 自動處理時間範圍限制（不超過24小時）
    - 返回包含降雨概率、溫度、體感溫度等資料

### 地震資料功能
- **獲取地震觀測資料**: 獲取特定地區的地震觀測資料
    - 預設返回過去36小時內的資料
    - 支持自定義時間範圍查詢
    - 自動調整過長的時間範圍（不超過36小時）

## 技術需求

### 開發環境
- **語言**: Kotlin
- **JDK 版本**: 21
- **構建工具**: Gradle
- **框架**: Quarkus

### 依賴庫
- Quarkus Kotlin
- MCP Server
- Jackson (用於 JSON 處理)
- MicroProfile REST Client (用於 API 調用)

## 設置與配置

### 環境變量設置
- `AUTH_KEY`: 台灣中央氣象署 API 授權密鑰

### 構建與運行

使用 Gradle 構建並運行服務:

```bash
./gradlew build
java -jar build/quarkus-app/quarkus-run.jar
```

開發模式:

```bash
./gradlew quarkusDev
```

### MCP 配置文件設置

您可以在 MCP 配置文件中添加 Google Drive 服務，方便統一管理多個 MCP 服務:

```json
{
  "mcpServers": {
    "gdrive": {
      "command": "java",
      "args": [
        "-jar", "D:\\MCP\\cwa-tw-1.0-SNAPSHOT-runner.jar"
      ],
      "env":{
        "AUTH_KEY": "${auth_key}"
      }
    }
  }
}
```

## 與 Langchain4j 集成示例

以下是在 LLMResource 中與氣象資訊工具集成的示例:

```kotlin
val weatherTransport = StdioMcpTransport.Builder()
    .command(listOf("java", "-jar", "cwa-tw-runner.jar"))
    .logEvents(true)
    .build()
val weatherClient = DefaultMcpClient.Builder().transport(weatherTransport).build()
val toolProvider = McpToolProvider.builder().mcpClients(listOf(weatherClient)).build()
aiService.toolProvider(toolProvider)
```

使用 JBang 運行:

```kotlin
val weatherTransport = StdioMcpTransport.Builder()
    .command(listOf("jbang", "--quiet", "cwa-tw-1.0-SNAPSHOT-runner.jar"))
    .logEvents(true)
    .build()
val weatherClient = DefaultMcpClient.Builder().transport(weatherTransport).build()
val toolProvider = McpToolProvider.builder().mcpClients(listOf(weatherClient)).build()
aiService.toolProvider(toolProvider)
```

## 備註

- 氣象資料來源於台灣中央氣象署開放資料平台
- 時間格式統一為 `yyyy-MM-ddThh:mm:ss`
- 當查詢超出合理時間範圍時，服務會自動進行調整以符合 API 限制
- 所有資料以 JSON 格式返回
