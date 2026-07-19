---
paths:
  - "gmail/**"
---

# gmail 模組規則

Gmail 郵件收發與管理，共 7 個工具。

## 設定

- 環境變數 `GMAIL_CREDENTIALS_FILE_PATH`：OAuth client secret JSON 的路徑
- **注意與 `google-drive` 的變數名不對稱**：此模組有 `GMAIL_` 前綴，google-drive 的是裸的 `CREDENTIALS_FILE_PATH`。兩者同時設定時容易搞混
- OAuth token 存於 `gmail_tokens/`（常數 `TOKENS_DIRECTORY_PATH`），相對於行程工作目錄建立，已列入 gitignore

## 需要的 OAuth scope

```
https://www.googleapis.com/auth/gmail_send
https://www.googleapis.com/auth/gmail_readonly
https://www.googleapis.com/auth/gmail_modify
```

新增工具若需要額外權限，改 scope 後既有的 `gmail_tokens/` 會失效，使用者需重新授權。這是破壞性變更，要在說明中告知。

## 郵件處理

- 寄信走標準 MIME（Jakarta Mail），支援 CC 與多個附件
- `deleteEmail` 是**移至垃圾桶**，不是永久刪除
- 搜尋使用 Gmail 原生查詢語法，直接透傳給 API，不要自行解析或改寫

## 回傳慣例

Gson 序列化 JSON 字串，透過 `createSuccessResponse()` / `createErrorResponse()` 包裝。

## 測試

`GmailOperationsIntegrationTest` 會**實際寄出郵件**，執行前務必確認：

- `GMAIL_CREDENTIALS_FILE_PATH` 已設定且已完成 OAuth 授權
- 測試中的 `testEmailAddress` 硬編為 `test@example.com`，需改成自己的測試信箱
- 用 `@TestMethodOrder` 串接情境（寄信 → 搜尋 → 標記已讀／未讀 → 刪除），測試之間有順序相依，不可單獨跑中間某個方法

沒有 `@EnabledIfEnvironmentVariable` 守衛，缺憑證時會直接失敗而非跳過。
