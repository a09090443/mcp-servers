# Google Maps MCP 服務器

此 MCP 服務器提供與 Google Maps 整合的功能，允許您搜索地點、獲取位置詳情、計算距離等操作，豐富 LLM 應用的地理和位置服務能力。

## 功能列表

### 地點搜索
- **文字搜索地點**: 通過關鍵詞搜索地點
- **尋找附近地點**: 基於位置搜索特定類型的附近地點
- **精確查找地點**: 通過文字或電話號碼精確查找地點

### 地點詳情
- **獲取地點詳情**: 根據地點 ID 獲取完整的地點信息
- **獲取地點照片**: 獲取地點照片的 URL

### 距離與位置
- **計算距離與時間**: 計算兩點間的距離和旅行時間
- **支援不同交通方式**: 支援駕車、步行、自行車和公共交通

### 輔助功能
- **獲取地點類型列表**: 列出所有可用的地點類型及其描述

## 技術需求

### 開發環境
- **語言**: Kotlin 2.0.21
- **JDK 版本**: 21
- **構建工具**: Gradle
- **框架**: Quarkus 3.21.0

### 依賴庫
- Quarkus Kotlin
- MCP Server STDIO v1.1.1
- Google Maps Services v2.2.0
- Google API Client v2.2.0
- Google HTTP Client v1.46.3

## 設置與配置

### 前提條件
1. [創建 Google Cloud 項目](https://console.cloud.google.com/projectcreate)
2. [啟用 Google Maps Platform API](https://console.cloud.google.com/google/maps-apis/api-list)
    - Places API
    - Distance Matrix API
    - Directions API
3. [創建 API 密鑰](https://console.cloud.google.com/google/maps-apis/credentials)

### 環境變量設置
- `GOOGLE_MAPS_API_KEY`: 您的 Google Maps API 密鑰

### 構建與運行

使用 Gradle 構建並運行服務:

```bash
./gradlew build
java -jar build/quarkus-app/quarkus-run.jar
```

開發模式:

```bash
./gradlew quarkusDev
```

### MCP 配置文件設置

您可以在 MCP 配置文件中添加 Google Maps 服務:

```json
{
  "mcpServers": {
    "gmaps": {
      "command": "java",
      "args": [
        "-jar", "D:\\MCP\\google-map-1.0-SNAPSHOT-runner.jar"
      ],
      "env": {
        "GOOGLE_MAPS_API_KEY": "${GOOGLE_MAPS_API_KEY}"
      }
    }
  }
}
```

配置說明:
- 此設定將 Google Maps MCP 服務命名為 `gmaps`
- `command` 指定要執行的命令 (此處為 java)
- `args` 包含執行命令的參數，指向 Google Maps MCP 服務的 JAR 文件
- 請根據您的實際環境調整 JAR 文件的路徑

## 與 Langchain4j 集成示例

以下是在應用中集成 Google Maps 工具的示例:

```kotlin
val googleMapsTransport = StdioMcpTransport.Builder()
    .command(listOf("java", "-jar", "google-map-runner.jar"))
    .environment(
        mapOf(
            "GOOGLE_MAPS_API_KEY" to System.getenv("GOOGLE_MAPS_API_KEY")
        )
    )
    .logEvents(true)
    .build()
val googleMaps = DefaultMcpClient.Builder().transport(googleMapsTransport).build()
val toolProvider = McpToolProvider.builder().mcpClients(listOf(googleMaps)).build()
aiService.toolProvider(toolProvider)
```

## 功能詳細說明

### 搜索地點相關功能

- `searchPlaces`: 通過文字關鍵詞搜索地點，可選指定搜索中心位置和半徑
- `getNearbyPlaces`: 搜索特定位置附近的地點，支援按地點類型過濾
- `findPlaceFromText`: 通過文字或電話號碼精確查找特定地點

### 地點詳情功能

- `getPlaceDetails`: 獲取地點的詳細信息，包含評分、評論、電話、網址等
- `getPlacePhotoUrl`: 獲取地點照片的 URL

### 距離與導航功能

- `calculateDistance`: 計算兩點間的距離和旅行時間，支援不同交通方式

### 輔助功能

- `getAvailablePlaceTypes`: 獲取所有可用的地點類型及其中文描述

## 權限與安全

此服務需要以下 Google Maps API 權限:
- Places API
- Distance Matrix API
- Directions API

請確保您的 API 密鑰具有這些 API 的訪問權限，並實施適當的使用限制和配額管理。
