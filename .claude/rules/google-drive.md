---
paths:
  - "google-drive/**"
---

# google-drive 模組規則

Google Drive 檔案與資料夾管理，共 22 個工具（全 repo 第二多）。

## 設定

- 環境變數 `CREDENTIALS_FILE_PATH`：OAuth client secret JSON 的路徑
- **沒有 `GOOGLE_` 或 `DRIVE_` 前綴**，與 `gmail` 模組的 `GMAIL_CREDENTIALS_FILE_PATH` 命名不一致，兩者同時設定時容易搞混
- OAuth token 存於 `tokens/`（常數 `TOKENS_DIRECTORY_PATH`），相對於行程工作目錄建立，已列入 gitignore

## SSLUtil.kt

此檔案提供 SSL/TLS 設定輔助。修改前先確認是否會關閉憑證驗證——放寬 TLS 會影響所有對 Google API 的連線。若只是為了排除本機連線問題，優先找其他解法。

## 回傳慣例

此模組用**擴充函式**風格，與 `date`／`excel`／`filesystem`／`gmail` 的 `createSuccessResponse()` 函式風格不同：

```kotlin
private fun Map<String, Any?>.toSuccessResponse(): String
```

搭配統一的錯誤處理包裝。新增工具時沿用擴充函式風格，不要混入另一種寫法。

## 工具數量已多，先查再加

22 個工具中已涵蓋檔案 CRUD、上傳下載、目錄管理、搜尋（全域與指定目錄）、依檔名查 ID、存在性檢查。新增功能前先確認是否已有近似工具，避免產生語意重疊的 API 讓呼叫端難以選擇。

## 測試

`GoogleDriveFileOperationsTest` 需要真實憑證且會操作實際的 Drive 內容（包含建立與刪除）。執行前確認用的是測試帳號，不要對正式資料跑。
