---
id: index
title: tw-stock
sidebar_label: 概覽
---

# tw-stock

台灣證交所（TWSE）開放資料，**28 個工具 + 5 個 prompt**，是 repo 中規模最大、結構最完整的模組。

## 目錄結構

唯一有套件分層的模組，新增程式碼請放對位置：

```
tw-stock/
├── build.gradle.kts                        # 相依：quarkus-rest-client(+jackson)、gson、
│                                           #       microprofile-rest-client-api、mockito-core（未實際使用）
├── API.md                                  # TWSE 端點對照表
├── README.md
└── src/
    ├── main/
    │   ├── docker/                         # Dockerfile.jvm / .legacy-jar / .native / .native-micro
    │   ├── kotlin/tw/zipe/mcp/twse/
    │   │   ├── TWStockClient.kt            # @RegisterRestClient → openapi.twse.com.tw/v1，一端點一方法
    │   │   ├── tool/TWStockTool.kt         # 28 個 @Tool（類別名為 TWStock，與檔名不同）
    │   │   ├── prompt/TWStockPrompt.kt     # 5 個 @Prompt —— 全 repo 唯一
    │   │   └── enumerate/IndustryCategory.kt   # 產業別列舉
    │   └── resources/
    │       └── application.properties      # uber-jar；日誌 → D:/tmp/tw-stock.log
    ├── test/kotlin/tw/zipe/mcp/twse/tool/
    │   └── TWStockToolTest.kt              # @QuarkusTest，直接打線上 API
    └── native-test/kotlin/tw/zipe/mcp/stock/
        └── ExampleResourceIT.kt            # 樣板殘留；套件名是 stock 而非 twse
```

## 設定

**無環境變數**，TWSE 開放資料不需金鑰。base URI 寫死在 `TWStockClient.kt` 的 `@RegisterRestClient(baseUri = "https://openapi.twse.com.tw/v1")`。

## 本節導覽

- [工具](./tools.md) —— 28 個 `@Tool` 的完整清單
- [Prompt 範本](./prompts.md) —— 5 個投資分析範本與參數格式
- [實作慣例](./conventions.md) —— client 端過濾模式與欄位命名陷阱
- [建置與測試](./development.md) —— 建置指令、測試特性、既有不一致
