---
id: tools
title: 工具
sidebar_label: 工具
---

# 工具

共 12 個 `@Tool`，全部定義在 `ExcelFileOperations.kt`。

新增工具前請先讀 [實作慣例](./conventions.md) 的資源管理一節——POI 的 `Workbook` 未關閉會在 Windows 上鎖住檔案。

## 檔案操作

| 工具 | 說明 |
|---|---|
| `createExcelFile` | 建立新的 Excel 活頁簿 |

## 工作表操作

| 工具 | 說明 |
|---|---|
| `addWorksheet` | 在活頁簿中建立新的工作表 |
| `listWorksheets` | 列出活頁簿中的所有工作表 |
| `deleteWorksheet` | 從活頁簿中移除工作表 |
| `renameWorksheet` | 重新命名工作表 |

## 資料操作

| 工具 | 說明 |
|---|---|
| `readCellData` | 讀取指定儲存格的值 |
| `writeCellData` | 向指定儲存格寫入值 |
| `readRowData` | 獲取整行的資料 |
| `writeRowData` | 寫入整行資料 |
| `mergeCells` | 合併儲存格範圍 |

## CSV 匯入匯出

| 工具 | 說明 |
|---|---|
| `importCSV` | 將 CSV 檔案資料匯入到 Excel 工作表 |
| `exportToCSV` | 將 Excel 工作表資料匯出為 CSV 檔案 |
