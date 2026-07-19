---
paths:
  - "tw-stock/**"
---

# tw-stock 模組規則

台灣證交所（TWSE）開放資料，30 個工具 + 5 個 prompt，是 repo 中規模最大、結構最完整的模組。

## 設定

**無環境變數**，TWSE 開放資料不需金鑰。base URI 寫死在 `TWStockClient.kt` 的 `@RegisterRestClient(baseUri = "https://openapi.twse.com.tw/v1")`。

## 分層結構

唯一有分層的模組，新增程式碼請放對位置：

```
tw/zipe/mcp/twse/
├── TWStockClient.kt        # @RegisterRestClient 介面，一個端點一個方法
├── tool/TWStockTool.kt     # @Tool 進入點（類別名為 TWStock，與檔名不同）
├── prompt/TWStockPrompt.kt # @Prompt 範本
└── enumerate/IndustryCategory.kt
```

## TWSE 沒有單一代號查詢端點

**所有端點一律回傳全市場資料**，過濾在 client 端做。既有慣例：

```kotlin
private const val CODE_KEY: String = "公司代號"   // 中文 key

val data = twStockClient.getCompanyBasicInfo()
return data.filter { it[CODE_KEY] == code }.ifEmpty {
    if (!code.isNullOrBlank()) emptyList() else data
}
```

語意是「代號有給但查無資料 → 空清單；代號空白 → 回傳全部」。新增工具請沿用這個模式。

**但不要假設每個端點的代號欄位都叫 `公司代號`。** 不同 TWSE 端點的欄位命名不一致，新增工具前先確認實際回傳結構（可參考 `API.md`）。

## 唯一提供 @Prompt 的模組

`TWStockPrompt.kt` 有 5 個繁體中文投資分析範本：股息投資策略、外資投資分析、投資篩選、市場熱點監控、個股趨勢分析。`@PromptArg` 的 description 採用 `key:中文說明` 的列舉格式，例如：

```kotlin
description = "high_yield:高收益／高殖利率, growth:成長／增長, timing:時機／入場時點"
```

新增 prompt 參數時沿用這個格式。

## 既有不一致（不必順手修）

- `tool/TWStockTool.kt` 的類別名是 `TWStock`，與檔名不符
- `getCompanyBusinessData` 已被註解停用，但 `TWStockClient` 中對應的方法仍保留
- `mockito-core` 列在依賴中但完全沒被使用

這些是既有狀況，除非任務本身就是要整理，否則不要順手改動而擴大 diff。

## 測試會打線上 API

`TWStockToolTest` 是 `@QuarkusTest` + `@Inject` 真實 bean，直接連線上 TWSE，**沒有 mock**。因為線上資料會隨開盤狀態變動，斷言刻意寫得寬鬆：

```kotlin
if (result.isNotEmpty()) { ... }
assertTrue(result.isEmpty() || result.all { it["公司代號"] == "2330" })
```

**新增測試請沿用這種防禦式寫法。** 改成嚴格斷言會在休市、資料未更新或個股下市時隨機失敗——那不是程式錯誤。
