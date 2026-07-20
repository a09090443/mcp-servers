---
id: filesystem
title: filesystem
sidebar_label: filesystem
---

# filesystem

本機檔案系統操作，共 **10 個工具**。

## 目錄結構

```
filesystem/
├── build.gradle.kts                        # 相依：gson、mockk（測試）
├── README.md
└── src/
    ├── main/
    │   ├── docker/                         # Dockerfile.jvm / .legacy-jar / .native / .native-micro
    │   ├── kotlin/tw/zipe/mcp/filesystem/
    │   │   ├── FileSystemApplication.kt    # @QuarkusMain —— 全 repo 唯一，解析 CLI 允許路徑
    │   │   └── FileSystemOperations.kt     # @Tool 進入點
    │   └── resources/
    │       └── application.properties      # uber-jar；日誌 → D:/tmp/filesystem.log
    └── test/kotlin/tw/zipe/mcp/filesystem/
        └── FileSystemOperationsTest.kt     # 可離線執行
```

## 工具

| 工具 | 說明 |
|---|---|
| `checkFileExists` | 檢查指定路徑的檔案或目錄是否存在 |
| `createFile` | 在指定路徑建立新檔案並寫入內容 |
| `readFile` | 讀取指定檔案的內容 |
| `updateFile` | 修改指定檔案的內容 |
| `deleteFile` | 刪除指定的檔案或目錄 |
| `copyFile` | 將檔案從一個路徑複製到另一個路徑 |
| `moveFile` | 將檔案從一個路徑移動到另一個路徑 |
| `createDirectory` | 建立目錄 |
| `listDirectory` | 瀏覽指定目錄中的檔案和子目錄 |
| `getFileInfo` | 取得檔案或目錄的詳細資訊 |

## 唯一有 `@QuarkusMain` 的模組

其他 7 個模組直接依賴 Quarkus 預設啟動流程，只有此模組自訂進入點 `FileSystemApplication`：

```kotlin
@QuarkusMain
class FileSystemApplication : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        if (args.isEmpty() || args.all { it == null }) return 1  // 無參數即終止
        System.setProperty("fileserver.paths", args.filterNotNull().joinToString(","))
        Quarkus.waitForExit()
        return 0
    }
}
```

## 啟動方式與其他模組不同

**必須傳入允許存取的路徑清單**：

```bash
./gradlew :filesystem:build
java -jar filesystem/build/filesystem-1.0-SNAPSHOT-runner.jar D:/allowed/path1 D:/allowed/path2
```

服務只能在這些指定路徑及其子目錄下操作檔案。

:::danger 不給參數會以 exit code 1 終止
這是刻意的安全設計——沒有明確授權路徑就不啟動。
:::

## MCP 設定

```json
{
  "mcpServers": {
    "filesystem": {
      "command": "java",
      "args": [
        "-jar", "path/to/filesystem-1.0-SNAPSHOT-runner.jar",
        "/path/to/allowed/directory",
        "/another/allowed/path"
      ]
    }
  }
}
```

## 修改路徑限制時要同步兩處

允許路徑的邏輯橫跨兩個檔案：

- `FileSystemApplication`：接收參數並寫入 `fileserver.paths` system property
- `FileSystemOperations`：讀取並驗證

只改一邊會造成限制失效或啟動失敗。這是此模組唯一的存取控制機制，放寬相關邏輯前要確認是否真的必要。

## 實作慣例

**回傳格式**：Gson 序列化 JSON 字串，透過 `createSuccessResponse()` / `createErrorResponse()` 包裝。

## 測試

`FileSystemOperationsTest` 為純本機測試，可離線執行。

```bash
./gradlew :filesystem:test
```
