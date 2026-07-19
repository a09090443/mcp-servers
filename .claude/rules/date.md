---
paths:
  - "date/**"
---

# date 模組規則

日期與時區處理，共 6 個工具。

## 特性

**全 repo 唯一無外部依賴、無環境變數的模組**，純 `java.time` 運算。因此：

- 測試可完全離線執行，`./gradlew :date:test` 不需任何前置設定
- 是驗證建置流程或 MCP 連線是否正常時最適合的模組
- 新增與日期／時區相關的通用功能時優先放這裡，不要散到其他模組

## 回傳慣例

用 Gson 序列化成 JSON 字串，經由 `createSuccessResponse()` / `createErrorResponse()` 兩個私有函式包裝，格式為 `{success, data}` / `{error, message, data}`。新增工具請沿用，不要直接回傳裸字串或 Kotlin 型別。

## 時區處理

工具接受時區 ID 字串（如 `Asia/Taipei`），無效 ID 應回傳錯誤回應而非拋例外。預設日期格式為 `yyyy-MM-dd HH:mm:ss`。
