---
id: google-map
title: google-map
sidebar_label: google-map
---

# google-map

Google Places API (New) 地點查詢，共 **7 個工具**。

## 目錄結構

```
google-map/
├── build.gradle.kts                        # 相依：google-api-client、google-http-client(+gson)、
│                                           #       google-maps-places、google-auth-library-oauth2-http、mockk
├── README.md
└── src/
    ├── main/
    │   ├── docker/                         # Dockerfile.jvm / .legacy-jar / .native / .native-micro
    │   ├── kotlin/tw/zipe/mcp/googlemap/places/
    │   │   ├── GoogleMapsPlacesOperations.kt               # @Tool 進入點
    │   │   └── RemoveTrailingUnderscoreNamingStrategy.kt   # 欄位命名轉換
    │   └── resources/
    │       └── application.properties      # uber-jar；日誌 → D:/tmp/google-map.log
    └── test/kotlin/tw/zipe/mcp/googlemap/places/
        └── GoogleMapsPlacesOperationsIntegrationTest.kt    # 需真實金鑰，執行會產生計費
```

## 工具

| 工具 | 說明 |
|---|---|
| `searchPlaces` | 文字查詢搜尋地點，可依評分、價位等級、地點類型過濾 |
| `getNearbyPlaces` | 依座標與半徑搜尋附近地點 |
| `getPlaceAutocomplete` | 地點名稱自動完成建議 |
| `getPlaceDetails` | 依 Place ID 獲取地點詳細資訊 |
| `getPlacePhoto` | 獲取地點照片 |
| `getPlacePhotosByPlaceId` | 只給 Place ID 一次取回照片 URL（內部自動解析照片資源名稱） |
| `getFieldMaskDescription` | 查詢可用的 field mask 欄位說明 |

## 設定

### 前置條件

1. [建立 Google Cloud 專案](https://console.cloud.google.com/projectcreate)
2. [啟用 Places API (New)](https://console.cloud.google.com/google/maps-apis/api-list)
3. [建立 API 金鑰](https://console.cloud.google.com/google/maps-apis/credentials)

### 環境變數

| 變數 | 說明 |
|---|---|
| `GOOGLE_MAPS_API_KEY` | Google Maps API 金鑰（常數 `API_KEY_ENV_VAR`），缺少時建構子直接拋 `IllegalArgumentException` |

## 建置與執行

```bash
./gradlew :google-map:build
java -jar google-map/build/google-map-1.0-SNAPSHOT-runner.jar
```

## MCP 設定

```json
{
  "mcpServers": {
    "gmaps": {
      "command": "java",
      "args": ["-jar", "D:\\MCP\\google-map-1.0-SNAPSHOT-runner.jar"],
      "env": {
        "GOOGLE_MAPS_API_KEY": "${GOOGLE_MAPS_API_KEY}"
      }
    }
  }
}
```

## Field mask 是必經路徑

Places API (New) **強制要求** `x-goog-fieldmask` header，缺少會直接回錯。因為每個工具需要的欄位不同，此模組每次呼叫都建立帶有該次專屬 header 的新 client：

```kotlin
private fun createPlacesClientWithFieldMask(fields: String?, forPlaceDetails: Boolean = false): PlacesClient
```

:::danger
新增工具時務必經由這個函式取得 client。直接使用注入的 `placesClient` 會因缺少 field mask 而失敗。
:::

## 前綴規則不一致

欄位前綴的處理有個分支，集中在 `addPlacesPrefix()`：

| 情境 | 前綴 |
|---|---|
| `getPlaceDetails` | 欄位**不加**前綴（`forPlaceDetails = true`） |
| 其餘方法 | 欄位需要 `places.` 前綴 |

這是 Google API 本身的不一致，不是實作瑕疵。新增工具時要判斷屬於哪一類，並把正確的 `forPlaceDetails` 傳下去。

## RemoveTrailingUnderscoreNamingStrategy

用於處理 Google API 產生的欄位名尾底線（如 `displayName_`）。序列化行為異常時先看這裡。

## 實作慣例

用擴充函式 `Any.toSuccessResponse()` 搭配 `executeWithErrorHandling { }` inline 包裝，與 [`google-drive`](./google-drive/conventions.md) 同風格：

```kotlin
private inline fun <T> executeWithErrorHandling(
    errorContext: Map<String, Any?> = emptyMap(),
    operation: () -> T?
): String
```

新增工具請包在 `executeWithErrorHandling` 內，錯誤才會被統一轉成 `{error, message, data}` 格式而非拋出。

## 自我描述工具

`getFieldMaskDescription()` 回傳所有可用欄位的說明，供呼叫端查詢後再決定 `fields` 參數。新增可用欄位時要同步更新這個工具的回傳內容，否則呼叫端查不到。

## 已知問題

Quarkus MCP server lib 目前有編碼問題，導致 LLM 取得的 response 可能出現亂碼。已回報上游。

## 測試會產生實際計費

:::danger
`GoogleMapsPlacesOperationsIntegrationTest` 需要有效的 API key，且 Places API 按請求計費。跑測試前留意配額。
:::
