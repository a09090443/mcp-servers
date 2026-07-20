---
id: tools
title: 工具
sidebar_label: 工具
---

# 工具

共 28 個 `@Tool`，全部定義在 `tool/TWStockTool.kt`（類別名為 `TWStock`）。

所有工具都受 [client 端過濾慣例](./conventions.md) 約束——TWSE 端點一律回傳全市場資料，代號過濾在程式內做。

## 公司資訊

| 工具 | 說明 |
|---|---|
| `getCompanyBasicInfo` | 公司基本資料 |
| `getCompanyAnnouncements` | 重大訊息公告 |
| `getCompanyDailyAnnouncements` | 每日重大訊息 |
| `getCompanyMonthlyRevenue` | 月營收 |
| `getCompanyDividendInfo` | 股利分派 |
| `getCompanyFinancialData` | 財務資料 |
| `getCompanyIncomeStatement` | 損益表 |
| `getCompanyBalanceSheet` | 銀行業資產負債表 |

## ESG 資訊

| 工具 | 說明 |
|---|---|
| `getCompanyESGSustainabilityReport` | 永續報告書 |
| `getCompanyESGFunctionalCommittee` | 功能性委員會 |
| `getCompanyESGSupplyChain` | 供應鏈管理 |
| `getCompanyESGRiskManagement` | 風險管理 |
| `getCompanyESGOtherInfo` | 其他 ESG 揭露 |

## 市場行情

| 工具 | 說明 |
|---|---|
| `getMarketIndices` | 大盤指數 |
| `getIndices5MinHistory` | 5 分鐘指數（歷史） |
| `getIndices5MinRealtime` | 5 分鐘指數（即時） |
| `getStockDailyInfo` | 個股每日成交資訊 |
| `getStockDailyAverage` | 個股每日均價 |
| `getTop48StocksInfo` | 熱門前 48 檔個股 |
| `getETFRanking` | ETF 排行 |
| `getStockValuationRatios` | 估值指標（本益比、殖利率等） |

## 信用交易與外資

| 工具 | 說明 |
|---|---|
| `getMarginTradingBalance` | 融資融券餘額 |
| `getMarginBuyingBalance` | 融資買進餘額 |
| `getShortSellingBalance` | 融券賣出餘額 |
| `getQFIICategories` | 外資持股類別 |
| `getTop20InvestorHoldingsSummary` | 外資前 20 大持股 |

## 市場動態

| 工具 | 說明 |
|---|---|
| `getNewsList` | 新聞列表 |
| `getEventList` | 市場事件列表 |
