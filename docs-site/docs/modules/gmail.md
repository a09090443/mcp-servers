---
id: gmail
title: gmail
sidebar_label: gmail
---

# gmail

Gmail 郵件收發與管理，共 **7 個工具**。

## 目錄結構

```
gmail/
├── build.gradle.kts                        # 相依：gson、google-api-services-gmail、google-api-client、
│                                           #       google-oauth-client-jetty、angus-mail
├── README.md
└── src/
    ├── main/
    │   ├── docker/                         # Dockerfile.jvm / .legacy-jar / .native / .native-micro
    │   ├── kotlin/tw/zipe/mcp/gmail/
    │   │   └── GmailOperations.kt          # 全部 @Tool 集中於此單一檔案
    │   └── resources/
    │       └── application.properties      # uber-jar；日誌 → D:/tmp/gmail.log
    └── test/kotlin/tw/zipe/mcp/gmail/
        └── GmailOperationsIntegrationTest.kt   # 需真實憑證，且會實際寄出郵件
```

## 工具

| 工具 | 說明 |
|---|---|
| `sendEmail` | 寄送郵件，支援 CC 與多個附件 |
| `listUnreadEmails` | 列出收件匣未讀郵件 |
| `searchEmails` | 使用 Gmail 原生搜尋語法查詢郵件 |
| `getEmail` | 依郵件 ID 獲取完整郵件詳情 |
| `markAsRead` | 標記郵件為已讀 |
| `markAsUnread` | 標記郵件為未讀 |
| `deleteEmail` | 將郵件移至垃圾桶 |

## 設定

### 前置條件

1. [在 Google Cloud Console 建立專案並啟用 Gmail API](https://console.cloud.google.com/apis/library/gmail.googleapis.com)
2. [設定 OAuth 同意畫面](https://console.cloud.google.com/apis/credentials/consent)
3. [建立 OAuth 用戶端 ID](https://console.cloud.google.com/apis/credentials)（應用類型選「桌面應用程式」）
4. 下載 OAuth 用戶端憑證 JSON 檔案

### 環境變數

| 變數 | 說明 |
|---|---|
| `GMAIL_CREDENTIALS_FILE_PATH` | OAuth client secret JSON 的路徑 |

:::warning 與 google-drive 的變數名不對稱
此模組有 `GMAIL_` 前綴，[`google-drive`](./google-drive/setup.md) 的是裸的 `CREDENTIALS_FILE_PATH`。兩者同時設定時容易搞混。
:::

OAuth token 存於 `gmail_tokens/`（常數 `TOKENS_DIRECTORY_PATH`），相對於行程工作目錄建立，已列入 gitignore。

## 需要的 OAuth scope

```
https://www.googleapis.com/auth/gmail_send
https://www.googleapis.com/auth/gmail_readonly
https://www.googleapis.com/auth/gmail_modify
```

:::danger 改 scope 是破壞性變更
新增工具若需要額外權限，改 scope 後既有的 `gmail_tokens/` 會失效，使用者需重新授權。要在說明中告知。
:::

## 建置與執行

```bash
./gradlew :gmail:build
java -jar gmail/build/gmail-1.0-SNAPSHOT-runner.jar
```

或用 JBang：`jbang --quiet gmail-1.0-SNAPSHOT-runner.jar`

## MCP 設定

```json
{
  "mcpServers": {
    "gmail": {
      "command": "java",
      "args": ["-jar", "D:\\MCP\\gmail-1.0-SNAPSHOT-runner.jar"],
      "env": {
        "GMAIL_CREDENTIALS_FILE_PATH": "${GMAIL_CREDENTIALS_FILE_PATH}"
      }
    }
  }
}
```

## 郵件處理

- 寄信走標準 MIME（Jakarta Mail），支援 CC 與多個附件
- `deleteEmail` 是**移至垃圾桶**，不是永久刪除
- 搜尋使用 Gmail 原生查詢語法，直接透傳給 API，不要自行解析或改寫

## 實作慣例

**回傳格式**：Gson 序列化 JSON 字串，透過 `createSuccessResponse()` / `createErrorResponse()` 包裝。

## 測試會實際寄出郵件

:::danger
`GmailOperationsIntegrationTest` 會**真的寄信**。執行前務必確認：

- `GMAIL_CREDENTIALS_FILE_PATH` 已設定且已完成 OAuth 授權
- 測試中的 `testEmailAddress` 硬編為 `test@example.com`，需改成自己的測試信箱
:::

用 `@TestMethodOrder` 串接情境（寄信 → 搜尋 → 標記已讀／未讀 → 刪除），測試之間有順序相依，不可單獨跑中間某個方法。

沒有 `@EnabledIfEnvironmentVariable` 守衛，缺憑證時會直接失敗而非跳過。
