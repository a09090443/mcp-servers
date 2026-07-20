---
id: getting-started
title: 快速開始
sidebar_position: 2
---

# 快速開始

## 前置需求

- **Java 21**（source/target 皆為 21）
- 不需另外安裝 Gradle，repo 內附 wrapper

## 建置

:::warning Gradle 指令一律在根目錄執行
子模組底下沒有 `gradlew`、`settings.gradle.kts` 或 `gradle.properties`，`cd` 進模組目錄是跑不動的。請用 `:模組名:任務` 指定對象。
:::

```bash
# 建置單一模組
./gradlew :tw-stock:build

# 建置全部八個模組（跳過測試）
./gradlew build -x test

# 列出已掛載的模組
./gradlew projects
```

建置產物落在各模組的 `build/` 底下，檔名格式為 `<模組>-1.0-SNAPSHOT-runner.jar`。

## 執行

```bash
java -jar tw-stock/build/tw-stock-1.0-SNAPSHOT-runner.jar
```

需要環境變數的模組（見[總覽表](./index.md#模組一覽)）在啟動時就會讀取，缺少時直接拋例外而非降級執行。

`filesystem` 的啟動方式與其他模組不同，必須傳入允許存取的路徑清單：

```bash
java -jar filesystem/build/filesystem-1.0-SNAPSHOT-runner.jar D:/allowed/path1 D:/allowed/path2
```

## 開發模式

Quarkus 的 live coding：

```bash
./gradlew :tw-stock:quarkusDev
```

## 設定到 MCP 用戶端

在 MCP 用戶端的設定檔中加入 server。以 `date`（無需環境變數）為例：

```json
{
  "mcpServers": {
    "date": {
      "command": "java",
      "args": ["-jar", "D:\\MCP\\date-1.0-SNAPSHOT-runner.jar"]
    }
  }
}
```

需要憑證的模組多一個 `env` 區塊：

```json
{
  "mcpServers": {
    "gdrive": {
      "command": "java",
      "args": ["-jar", "D:\\MCP\\google-drive-1.0-SNAPSHOT-runner.jar"],
      "env": {
        "CREDENTIALS_FILE_PATH": "${CREDENTIALS_FILE_PATH}"
      }
    }
  }
}
```

## 與 Langchain4j 整合

各模組皆可透過 `StdioMcpTransport` 掛進 Langchain4j 的 `AiService`：

```kotlin
val transport = StdioMcpTransport.Builder()
    .command(listOf("java", "-jar", "date-1.0-SNAPSHOT-runner.jar"))
    .logEvents(true)
    .build()
val client = DefaultMcpClient.Builder().transport(transport).build()
val toolProvider = McpToolProvider.builder().mcpClients(listOf(client)).build()
aiService.toolProvider(toolProvider)
```

需要環境變數的模組再串一段 `.environment(mapOf("AUTH_KEY" to System.getenv("AUTH_KEY")))`。也可改用 JBang 啟動：`.command(listOf("jbang", "--quiet", "<jar>"))`。

## 先驗證環境

`date` 模組無外部依賴、無環境變數、可完全離線執行，是驗證建置流程或 MCP 連線是否正常時最適合的起點：

```bash
./gradlew :date:test
```
