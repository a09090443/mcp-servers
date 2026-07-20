---
id: conventions
title: 實作慣例
sidebar_label: 實作慣例
---

# 實作慣例

## 回傳格式

Gson 序列化 JSON 字串，透過 `createSuccessResponse()` / `createErrorResponse()` 私有函式包裝成 `{success, data}` / `{error, message, data}`。

與 `date`／`filesystem`／`gmail` 同屬此風格；`google-drive` 與 `google-map` 用的是擴充函式風格，不要混用。

## 資源管理

:::warning
POI 的 `Workbook` 與檔案串流必須確實關閉，否則在 Windows 上會鎖住檔案導致後續操作失敗。新增工具時確認寫入路徑有 `use { }` 或等效的關閉處理。
:::

## 既有格式差異

`ExcelFileOperations.kt` 的 companion object 與其內部成員比其他模組多縮排一層。這是既有狀況，**不必特意對齊**，也不要為了統一風格而重排整個檔案——那會產生與功能無關的大量 diff。新增成員時跟隨該檔案當下的縮排。
