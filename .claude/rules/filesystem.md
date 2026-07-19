---
paths:
  - "filesystem/**"
---

# filesystem 模組規則

本機檔案系統操作，共 10 個工具。

## 唯一有 @QuarkusMain 的模組

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

**啟動方式因此與其他模組不同**，必須傳入允許存取的路徑清單：

```bash
java -jar filesystem/build/filesystem-1.0-SNAPSHOT-runner.jar D:/allowed/path1 D:/allowed/path2
```

不給參數會以 exit code 1 終止，這是刻意的安全設計——沒有明確授權路徑就不啟動。

## 修改路徑限制時要同步兩處

允許路徑的邏輯橫跨兩個檔案：`FileSystemApplication` 負責接收與寫入 `fileserver.paths` system property，`FileSystemOperations` 負責讀取並驗證。只改一邊會造成限制失效或啟動失敗。

這是此模組唯一的存取控制機制，放寬相關邏輯前要確認是否真的必要。

## 回傳慣例

Gson 序列化 JSON 字串，透過 `createSuccessResponse()` / `createErrorResponse()` 包裝。

## 測試

`FileSystemOperationsTest` 為純本機測試，可離線執行。
