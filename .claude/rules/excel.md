---
paths:
  - "excel/**"
---

# excel 模組規則

Apache POI 驅動的 Excel 檔案操作，共 12 個工具。

## 設定

無環境變數。所有工具接受本機檔案路徑作為參數，直接讀寫檔案系統。

## 既有格式問題

`ExcelFileOperations.kt` 的 companion object 與其內部成員比其他模組多縮排一層（`private val gson` 在第 22 行縮排 16 個空格）。這是既有狀況，**不必特意對齊**，也不要為了統一風格而重排整個檔案——那會產生與功能無關的大量 diff。新增成員時跟隨該檔案當下的縮排。

## 回傳慣例

Gson 序列化 JSON 字串，透過 `createSuccessResponse()` / `createErrorResponse()` 包裝。

## 資源管理

POI 的 `Workbook` 與檔案串流必須確實關閉，否則 Windows 上會鎖住檔案導致後續操作失敗。新增工具時確認寫入路徑有 `use { }` 或等效的關閉處理。

## 測試

`ExcelFileOperationsTest` 為純本機測試，可離線執行。測試會產生暫存 xlsx 檔案，注意清理。
