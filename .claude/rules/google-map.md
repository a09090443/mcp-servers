---
paths:
  - "google-map/**"
---

# google-map 模組規則

Google Places API (New) 地點查詢，共 6 個工具。

## 檔案結構

```
google-map/src/main/kotlin/tw/zipe/mcp/googlemap/places/
├── GoogleMapsPlacesOperations.kt
└── RemoveTrailingUnderscoreNamingStrategy.kt
```

## 設定

環境變數 `GOOGLE_MAPS_API_KEY`（常數 `API_KEY_ENV_VAR`），缺少時建構子直接拋 `IllegalArgumentException`。

## Field mask 是必經路徑

Places API (New) **強制要求** `x-goog-fieldmask` header，缺少會直接回錯。因為每個工具需要的欄位不同，此模組的做法是每次呼叫都建立帶有該次專屬 header 的新 client：

```kotlin
private fun createPlacesClientWithFieldMask(fields: String?, forPlaceDetails: Boolean = false): PlacesClient
```

**新增工具時務必經由這個函式取得 client。** 直接使用注入的 `placesClient` 會因缺少 field mask 而失敗。

臨時 client 用 try/finally 包住呼叫,例外時也保證 `close()`(否則洩漏 gRPC channel);常駐 `placesClient` 由 `@PreDestroy cleanup()` 關閉。

## maxResultCount 是目前唯一可用的筆數欄位

`searchPlaces` / `getNearbyPlaces` 用 `SearchTextRequest.setMaxResultCount()`。Google 官方已於 2024-05 將 Text Search 的 `maxResultCount` 標為 deprecated、建議改 `pageSize`,**但 `google-maps-places` 0.65.0(現行最新版)的 proto 尚未提供 `pageSize`/`pageToken`**(已用 `javap` 確認 `SearchTextRequest$Builder` 只有 `maxResultCount`)。因此維持 `maxResultCount` 是正確的,不要因為 deprecation 警告改寫;等函式庫補上 `pageSize` 再議。

## 前綴規則不一致

欄位前綴的處理有個分支，集中在 `addPlacesPrefix()`：

- `getPlaceDetails`：欄位**不加**前綴（`forPlaceDetails = true`）
- 其餘方法：欄位需要 `places.` 前綴

這是 Google API 本身的不一致，不是實作瑕疵。新增工具時要判斷屬於哪一類，並把正確的 `forPlaceDetails` 傳下去。

## RemoveTrailingUnderscoreNamingStrategy

用於處理 Google API 產生的欄位名尾底線（如 `displayName_`）。序列化行為異常時先看這裡。

## 回傳慣例

用擴充函式 `Any.toSuccessResponse()` 搭配 `executeWithErrorHandling { }` inline 包裝，與 `google-drive` 同風格：

```kotlin
private inline fun <T> executeWithErrorHandling(
    errorContext: Map<String, Any?> = emptyMap(),
    operation: () -> T?
): String
```

新增工具請包在 `executeWithErrorHandling` 內，錯誤才會被統一轉成 `{error, message, data}` 格式而非拋出。

## 自我描述工具

`getFieldMaskDescription()` 回傳所有可用欄位的說明，供呼叫端查詢後再決定 `fields` 參數。新增可用欄位時要同步更新這個工具的回傳內容，否則呼叫端查不到。

## 測試

`GoogleMapsPlacesOperationsIntegrationTest` 需要有效的 API key 且會**產生實際計費**。Places API 按請求計費，跑測試前留意配額。
