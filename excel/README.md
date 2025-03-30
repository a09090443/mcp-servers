# Excel MCP 服務器

此 MCP 服務器提供 Excel 文件操作功能，允許您建立、讀取、更新和管理 Excel 文件，支援 CSV 匯入匯出等功能。

## 功能列表

### 基礎文件操作
- **建立 Excel 文件**：建立新的 Excel 活頁簿
- **讀取 Excel 文件**：獲取現有 Excel 文件內容
- **寫入 Excel 文件**：向 Excel 文件寫入資料
- **檢查文件是否存在**：判斷指定路徑的 Excel 文件是否存在

### 工作表操作
- **建立工作表**：在 Excel 活頁簿中建立新的工作表
- **獲取工作表列表**：列出 Excel 活頁簿中的所有工作表
- **刪除工作表**：從 Excel 活頁簿中移除工作表
- **修改工作表名稱**：重命名 Excel 工作表

### 資料操作
- **讀取儲存格資料**：讀取指定儲存格的值
- **寫入儲存格資料**：向指定儲存格寫入值
- **讀取行資料**：獲取整行的資料
- **寫入行資料**：寫入整行資料
- **讀取列資料**：獲取整列的資料
- **寫入列資料**：寫入整列資料
- **讀取區域資料**：獲取指定區域的資料
- **寫入區域資料**：向指定區域寫入資料

### CSV 匯入匯出
- **匯入 CSV 文件**：將 CSV 文件資料匯入到 Excel 工作表
- **匯出為 CSV 文件**：將 Excel 工作表資料匯出為 CSV 文件

## 技術需求

### 開發環境
- **語言**：Kotlin
- **JDK 版本**：21
- **構建工具**：Gradle
- **框架**：Quarkus 3.x

### 主要依賴庫
- Quarkus Kotlin
- Apache POI 5.x：用於 Excel 文件操作
- OpenCSV：用於 CSV 文件操作
- MCP Server STDIO：用於 MCP 服務通信

## 設定與配置

### 構建與運行

使用 Gradle 構建並運行服務：

```bash
./gradlew build
java -jar build/quarkus-app/quarkus-run.jar
```

構建 über-jar：

```bash
./gradlew build -Dquarkus.package.jar.type=uber-jar
```

開發模式運行：

```bash
./gradlew quarkusDev
```

### MCP 配置文件設定

您可以在 MCP 配置文件中添加 Excel 服務：

```json
{
  "mcpServers": {
    "excel": {
      "command": "java",
      "args": [
        "-jar", "path/to/excel-1.0-SNAPSHOT-runner.jar"
      ]
    }
  }
}
```

配置說明：
- 此設定將 Excel MCP 服務命名為 `excel`
- `command` 指定要執行的命令
- `args` 包含執行命令的參數，指向 Excel MCP 服務的 JAR 文件
- 請根據您的實際環境調整 JAR 文件的路徑

## 與 Langchain4j 整合範例

以下是與 Langchain4j 整合的範例程式碼：

```kotlin
val excelTransport = StdioMcpTransport.Builder()
    .command(listOf("java", "-jar", "excel-1.0-SNAPSHOT-runner.jar"))
    .logEvents(true)
    .build()
val excelClient = DefaultMcpClient.Builder().transport(excelTransport).build()
val toolProvider = McpToolProvider.builder().mcpClients(listOf(excelClient)).build()
aiService.toolProvider(toolProvider)
```

## 日誌配置

日誌文件配置在 `application.properties` 中：

```ini
quarkus.package.type=uber-jar
quarkus.log.file.enable=true
quarkus.log.file.path=D:/tmp/excel.log
```
