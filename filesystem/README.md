# 文件File operation MCP 服務器

此 MCP 服務器提供文件系統操作功能，允許您進行文件讀寫、複製移動、目錄瀏覽等操作。服務啟動時需要指定允許操作的路徑範圍，確保系統安全。

## 功能列表

### 文件操作
- **檢查文件存在**：檢查指定路徑的文件或目錄是否存在
- **創建文件**：在指定路徑創建新文件並寫入內容
- **讀取文件**：讀取指定文件的內容
- **更新文件**：修改指定文件的內容
- **刪除文件**：刪除指定的文件或目錄

### 文件管理
- **複製文件**：將文件從一個路徑複製到另一個路徑
- **移動文件**：將文件從一個路徑移動到另一個路徑
- **列出目錄內容**：瀏覽指定目錄中的文件和子目錄
- **獲取文件信息**：取得文件或目錄的詳細資訊

## 技術需求

### 開發環境
- 語言：Kotlin
- JDK 版本：21
- 構建工具：Gradle
- 框架：Quarkus 3.x

### 主要依賴庫
- Quarkus Kotlin
- Java NIO：用於文件操作
- Gson：用於 JSON 格式化
- MCP Server STDIO：用於 MCP 服務通信

## 設定與配置

### 構建與運行
使用 Gradle 構建並運行服務：

```
./gradlew build
java -jar build/quarkus-app/quarkus-run.jar /path/to/allowed/directory,/another/allowed/path
```

構建 uber-jar：

```
./gradlew build -Dquarkus.package.jar.type=uber-jar
```

開發模式運行：

```
./gradlew quarkusDev /path/to/allowed/directory
```

### 啟動參數說明
- 服務啟動時必須提供一個或多個允許操作的路徑，例如：
```
java -jar build/quarkus-app/quarkus-run.jar C:/path/to/allowed/directory /another/allowed/path
```
- 這些路徑將被設置為系統屬性 `fileserver.paths`
- 服務只能在這些指定路徑及其子目錄下操作文件

### MCP 配置文件設定
您可以在 MCP 配置文件中添加文件系統服務：

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

配置說明：
- 此設定將文件系統 MCP 服務命名為 filesystem
- command 指定要執行的命令
- args 包含執行命令的參數，包括 JAR 文件路徑和允許操作的目錄列表
- 請根據您的實際環境調整 JAR 文件和允許目錄的路徑

### 與 Langchain4j 整合範例
以下是與 Langchain4j 整合的範例程式碼：

```kotlin
val filesystemTransport = StdioMcpTransport.Builder()
    .command(listOf("java", "-jar", "filesystem-1.0-SNAPSHOT-runner.jar", 
                   "/path/to/allowed/directory"))
    .logEvents(true)
    .build()
val filesystemClient = DefaultMcpClient.Builder().transport(filesystemTransport).build()
val toolProvider = McpToolProvider.builder().mcpClients(listOf(filesystemClient)).build()
aiService.toolProvider(toolProvider)
```

### 日誌配置
日誌文件配置在 application.properties 中：

```
quarkus.package.type=uber-jar
quarkus.log.file.enable=true
quarkus.log.file.path=D:/tmp/filesystem.log
```
