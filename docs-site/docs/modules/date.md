---
id: date
title: date
sidebar_label: date
---

# date

日期與時區處理，共 **6 個工具**。

**全 repo 唯一無外部依賴、無環境變數的模組**，純 `java.time` 運算。因此測試可完全離線執行，是驗證建置流程或 MCP 連線是否正常時最適合的模組。

## 目錄結構

```
date/
├── build.gradle.kts                       # 套用 mcp-server.conventions；相依：gson、mockito-core
├── README.md
└── src/
    ├── main/
    │   ├── docker/                        # Dockerfile.jvm / .legacy-jar / .native / .native-micro
    │   ├── kotlin/tw/zipe/mcp/date/
    │   │   └── DateZoneOperations.kt      # 6 個 @Tool，純 java.time 運算
    │   └── resources/
    │       └── application.properties     # uber-jar；日誌 → D:/tmp/date.log
    └── test/kotlin/tw/zipe/mcp/date/
        └── DateZoneOperationsTest.kt      # 可完全離線執行
```

## 工具

| 工具 | 說明 |
|---|---|
| `getTodayDate` | 獲取今天的日期，支援自定義格式 |
| `isToday` | 判斷指定日期是否為今天 |
| `getDateTimeInTimeZone` | 獲取指定時區的當前日期時間 |
| `convertBetweenTimeZones` | 在不同時區之間轉換日期時間 |
| `getAvailableTimeZones` | 列出系統支援的所有時區 |
| `getCommonTimeZonesByRegion` | 獲取指定地區（如亞洲、歐洲、美洲）的所有時區 |

## 設定

無環境變數，無外部依賴。

## 建置與執行

```bash
./gradlew :date:build
java -jar date/build/date-1.0-SNAPSHOT-runner.jar
```

## MCP 設定

```json
{
  "mcpServers": {
    "date": {
      "command": "java",
      "args": ["-jar", "path/to/date-1.0-SNAPSHOT-runner.jar"]
    }
  }
}
```

## 實作慣例

- **回傳格式**：Gson 序列化 JSON 字串，經 `createSuccessResponse()` / `createErrorResponse()` 私有函式包裝成 `{success, data}` / `{error, message, data}`。不要直接回傳裸字串或 Kotlin 型別。
- **時區處理**：工具接受時區 ID 字串（如 `Asia/Taipei`），無效 ID 應回傳錯誤回應而非拋例外。預設日期格式為 `yyyy-MM-dd HH:mm:ss`。

:::tip
新增與日期／時區相關的通用功能時優先放這裡，不要散到其他模組。
:::

## 測試

```bash
./gradlew :date:test    # 不需任何前置設定
```
