---
id: development
title: 建置與測試
sidebar_label: 建置與測試
---

# 建置與測試

## 建置與執行

```bash
./gradlew :tw-stock:build
java -jar tw-stock/build/tw-stock-1.0-SNAPSHOT-runner.jar
```

## 測試會打線上 API

`TWStockToolTest` 是 `@QuarkusTest` + `@Inject` 真實 bean，直接連線上 TWSE，**沒有 mock**。因為線上資料會隨開盤狀態變動，斷言刻意寫得寬鬆：

```kotlin
if (result.isNotEmpty()) { ... }
assertTrue(result.isEmpty() || result.all { it["公司代號"] == "2330" })
```

**新增測試請沿用這種防禦式寫法。** 改成嚴格斷言會在休市、資料未更新或個股下市時隨機失敗——那不是程式錯誤。

執行方式：

```bash
./gradlew :tw-stock:test                                        # 全部
./gradlew :tw-stock:test --tests "tw.zipe.mcp.twse.tool.TWStockToolTest"
```

## 既有不一致（不必順手修）

- `tool/TWStockTool.kt` 的類別名是 `TWStock`，與檔名不符
- `getCompanyBusinessData` 已被註解停用，但 `TWStockClient` 中對應的方法仍保留
- `mockito-core` 列在依賴中但完全沒被使用
- `src/native-test/` 下的 `ExampleResourceIT.kt` 套件名是 `tw.zipe.mcp.stock`，與其他檔案的 `twse` 不一致（Quarkus 樣板殘留）

這些是既有狀況，除非任務本身就是要整理，否則不要順手改動而擴大 diff。
