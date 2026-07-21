# Google Maps MCP 服務器

此 MCP 服務器基於 **Google Places API (New)**，提供地點搜尋、附近查詢、自動完成、地點詳情與照片等功能，豐富 LLM 應用的地理與位置服務能力。

## 功能列表

共 **6 個工具**：

### 地點搜尋
- **文字搜尋地點** (`searchPlaces`)：以關鍵詞搜尋，可依評分、價位等級、地點類型過濾，並可指定搜尋中心座標與半徑
- **搜尋附近地點** (`getNearbyPlaces`)：依座標與半徑搜尋附近地點，可按主要類型過濾、指定排序偏好（距離／熱門）
- **地點自動完成** (`getPlaceAutocomplete`)：依輸入文字回傳地點建議，可加入位置偏好

### 地點詳情
- **獲取地點詳情** (`getPlaceDetails`)：依 Place ID 取得完整資訊（電話、地址、營業時間、評分、評論、照片等）
- **獲取地點照片** (`getPlacePhoto`)：依照片資源名稱取得照片 URL

### 輔助功能
- **查詢 field mask 欄位說明** (`getFieldMaskDescription`)：列出所有可用欄位及其說明，供呼叫端決定 `fields` 參數

## 技術需求

### 開發環境
- **語言**: Kotlin 2.4.0
- **JDK 版本**: 21
- **構建工具**: Gradle
- **框架**: Quarkus 3.37.3

### 依賴庫
- Quarkus Kotlin
- MCP Server STDIO v1.13.1
- Google Maps Places v0.65.0
- Google API Client v2.9.0
- Google HTTP Client v1.47.1

## 設置與配置

### 前提條件
1. [建立 Google Cloud 專案](https://console.cloud.google.com/projectcreate)
2. [啟用 Places API (New)](https://console.cloud.google.com/google/maps-apis/api-list)
3. [建立 API 金鑰](https://console.cloud.google.com/google/maps-apis/credentials)

> 本服務僅使用 **Places API (New)**，不需要 Distance Matrix API 或 Directions API。

### 環境變量設置
- `GOOGLE_MAPS_API_KEY`: 您的 Google Maps API 金鑰（常數 `API_KEY_ENV_VAR`），缺少時建構子會直接拋 `IllegalArgumentException`

### 構建與運行

使用 Gradle 構建並運行服務（指令一律在 repo 根目錄執行）：

```bash
./gradlew :google-map:build
java -jar google-map/build/google-map-1.0-SNAPSHOT-runner.jar
```

開發模式：

```bash
./gradlew :google-map:quarkusDev
```

### MCP 配置文件設置

您可以在 MCP 配置文件中添加 Google Maps 服務：

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

配置說明：
- 此設定將 Google Maps MCP 服務命名為 `gmaps`
- `command` 指定要執行的命令（此處為 java）
- `args` 指向 Google Maps MCP 服務的 JAR 文件
- 請根據您的實際環境調整 JAR 文件的路徑

## 與 Langchain4j 集成示例

以下是在應用中集成 Google Maps 工具的示例：

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

### 地點搜尋

- `searchPlaces`：以文字關鍵詞搜尋地點，可選指定搜尋中心座標、半徑、語言、開放狀態、地點類型、最低評分與價位等級（`FREE`／`INEXPENSIVE`／`MODERATE`／`EXPENSIVE`／`VERY_EXPENSIVE`）
- `getNearbyPlaces`：搜尋指定座標附近的地點，可按主要類型過濾，並以 `DISTANCE` 或 `POPULARITY` 排序
- `getPlaceAutocomplete`：依輸入文字回傳自動完成建議，可加入座標與半徑作為位置偏好

### 地點詳情

- `getPlaceDetails`：依 Place ID 取得詳細資訊，包含評分、評論、電話、網址、營業時間、照片等
- `getPlacePhoto`：依照片資源名稱（`places/PLACE_ID/photos/PHOTO_REFERENCE`）取得照片 URL

### 輔助功能

- `getFieldMaskDescription`：回傳所有可用 field mask 欄位及其說明，供呼叫端在指定 `fields` 參數前查詢

## 欄位掩碼（Field Mask）

Places API (New) **強制要求** `x-goog-fieldmask` header 指定要回傳的欄位。本服務每次呼叫都會建立帶有該次專屬 header 的臨時 client；各工具的 `fields` 參數以逗號分隔欄位名（可先呼叫 `getFieldMaskDescription` 查詢可用欄位）。

## 權限與安全

此服務僅需 **Places API (New)** 權限。請確保 API 金鑰具備該 API 的存取權限，並實施適當的使用限制與配額管理。

## 已知問題

Quarkus MCP server lib 曾出現編碼問題，導致 LLM 取得的 response 中文可能出現亂碼，已回報上游。若在目前的 lib 版本（`quarkus-mcp-server-stdio` 1.13.1）仍重現，回應內容的中文會呈現亂碼；序列化本身（Gson + UTF-8）為正常。
