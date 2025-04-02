# Gmail MCP 服務器

此 MCP 服務器提供與 Gmail 的整合功能，讓使用者能夠輕鬆管理電子郵件。主要功能包含：

## 電子郵件管理功能

### 郵件收發操作
- **發送郵件**：發送電子郵件，支援添加副本和附件
- **列出未讀郵件**：獲取收件匣中的未讀電子郵件
- **搜尋郵件**：使用 Gmail 搜索語法查找特定電子郵件
- **獲取郵件內容**：根據郵件 ID 獲取完整郵件詳情

### 郵件狀態管理
- **標記為已讀**：將指定郵件標記為已讀狀態
- **標記為未讀**：將指定郵件標記為未讀狀態
- **刪除郵件**：將郵件移至垃圾桶，可永久刪除

## 特點與優勢
- **附件支援**：發送郵件時可添加多個附件
- **副本功能**：支援發送副本 (CC) 給多個收件人
- **完整郵件格式**：支援標準 MIME 郵件格式
- **強大的搜尋**：使用 Gmail 原生搜尋語法查找郵件
- **標準 JSON 回應**：所有操作均返回統一格式的 JSON 回應
- **完善的錯誤處理**：提供詳細錯誤信息和操作上下文

## 技術需求與依賴
- **語言**：Kotlin (版本 2.0.21)
- **JDK 版本**：21
- **構建工具**：Gradle
- **框架**：Quarkus 3.21.0
- **主要依賴庫**：Google API Client、Gmail API、Jakarta Mail、MCP Server STDIO

## 環境設置
1. [在 Google Cloud Console 創建專案並啟用 Gmail API](https://console.cloud.google.com/apis/library/drive.googleapis.com)
2. [配置 OAuth 同意屏幕](https://console.cloud.google.com/apis/credentials/consent)
3. [創建 OAuth 客戶端 ID](https://console.cloud.google.com/apis/credentials)（應用類型選擇"桌面應用"）
4. 下載 OAuth 客戶端憑證 JSON 文件

## 建置與執行
- 使用 Gradle 建置並執行服務：
  ```shell script
  ./gradlew build
  java -jar build/quarkus-app/quarkus-run.jar
  ```
- 使用 JBang 運行：
  ```shell script
  jbang --quiet gmail-1.0-SNAPSHOT-runner.jar
  ```
- 開發模式啟動：
  ```shell script
  ./gradlew quarkusDev
  ```

## MCP 服務配置範例
在 MCP 配置檔中，可新增 Gmail 服務，例如：
```json
{
  "mcpServers": {
    "gmail": {
      "command": "java",
      "args": [
        "-jar", "D:\\MCP\\gmail-1.0-SNAPSHOT-runner.jar"
      ],
      "env": {
        "GMAIL_CREDENTIALS_FILE_PATH": "${GMAIL_CREDENTIALS_FILE_PATH}"
      }
    }
  }
}
```

## 與 Langchain4j 整合示例
利用 MCP 客戶端與 Langchain4j 結合，示例如下：
```kotlin
val gmailTransport = StdioMcpTransport.Builder()
    .command(listOf("java", "-jar", "gmail-runner.jar"))
    .environment(
        mapOf(
            "GMAIL_CREDENTIALS_FILE_PATH" to System.getenv("GMAIL_CREDENTIALS_FILE_PATH")
        )
    )
    .logEvents(true)
    .build()
val gmail = DefaultMcpClient.Builder().transport(gmailTransport).build()
val toolProvider = McpToolProvider.builder().mcpClients(listOf(gmail)).build()
aiService.toolProvider(toolProvider)
```

或使用 JBang：
```kotlin
val gmailTransport = StdioMcpTransport.Builder()
    .command(listOf("jbang", "--quiet", "gmail-1.0-SNAPSHOT-runner.jar"))
    .environment(
        mapOf(
            "GMAIL_CREDENTIALS_FILE_PATH" to System.getenv("GMAIL_CREDENTIALS_FILE_PATH")
        )
    )
    .logEvents(true)
    .build()
val gmail = DefaultMcpClient.Builder().transport(gmailTransport).build()
val toolProvider = McpToolProvider.builder().mcpClients(listOf(gmail)).build()
aiService.toolProvider(toolProvider)
```

## 權限需求
此服務需授予 Gmail 以下權限：
- `https://www.googleapis.com/auth/gmail_send`：發送電子郵件的權限
- `https://www.googleapis.com/auth/gmail_readonly`：讀取電子郵件的權限
- `https://www.googleapis.com/auth/gmail_modify`：修改電子郵件標籤的權限
