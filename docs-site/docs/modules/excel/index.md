---
id: index
title: excel
sidebar_label: 概覽
---

# excel

Apache POI 驅動的 Excel 檔案操作，共 **12 個工具**。

## 目錄結構

```
excel/
├── build.gradle.kts                        # 相依：poi、poi-ooxml、gson
├── README.md
└── src/
    ├── main/
    │   ├── docker/                         # Dockerfile.jvm / .legacy-jar / .native / .native-micro
    │   ├── kotlin/tw/zipe/mcp/excel/
    │   │   └── ExcelFileOperations.kt      # 全部 @Tool 集中於此單一檔案
    │   └── resources/
    │       └── application.properties      # uber-jar；日誌 → D:/tmp/excel.log
    └── test/kotlin/tw/zipe/mcp/excel/
        └── ExcelFileOperationsTest.kt      # 可離線執行（在暫存目錄實際產生 .xlsx）
```

## 設定

無環境變數。所有工具接受本機檔案路徑作為參數，直接讀寫檔案系統。

## 主要依賴

- Apache POI 5.x：Excel 檔案操作
- OpenCSV：CSV 檔案操作

## 本節導覽

- [工具](./tools.md) —— 12 個 `@Tool` 的完整清單
- [實作慣例](./conventions.md) —— 回傳格式、POI 資源關閉、既有縮排差異
- [建置與測試](./development.md) —— 建置指令、MCP 設定、測試
