---
id: conventions
title: 實作慣例
sidebar_label: 實作慣例
---

# 實作慣例

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

:::warning 不要假設每個端點的代號欄位都叫 `公司代號`
不同 TWSE 端點的欄位命名不一致，新增工具前先確認實際回傳結構（可參考 `API.md`）。
:::

## 回傳型別

tw-stock 與 `cwa-tw` 同屬「直接回傳 Kotlin 型別」的慣例：回傳 `List<Map<String, Any>>` 或 `Map<String, Any>`，由框架序列化，**不使用 Gson 手動包裝**成 `{success, data}`。

新增工具時請對齊本模組既有寫法，不要引入其他模組的 `createSuccessResponse()` 風格。
