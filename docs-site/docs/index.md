---
id: index
title: 總覽
sidebar_label: 總覽
slug: /
---

# MCP Servers

這個 repo 收納 **8 個 MCP（Model Context Protocol）Server**，全部以 Kotlin + Quarkus 開發，透過 `quarkus-mcp-server-stdio` 以 STDIO transport 與 LLM 用戶端溝通。

專案以 **Gradle multi-module** 組織：共用單一 wrapper 與單一 version catalog，但各模組在執行期彼此獨立，每個都能單獨打包成可部署的 uber-jar。

## 模組一覽

| 模組 | 功能 | 工具數 | 需要環境變數 | 需要網路 |
|---|---|---|---|---|
| [`date`](./modules/date.md) | 日期與時區處理 | 6 | 否 | 否 |
| [`excel`](./modules/excel/index.md) | Excel 檔案操作（Apache POI） | 12 | 否 | 否 |
| [`filesystem`](./modules/filesystem.md) | 本機檔案系統 | 10 | 否（需 CLI 路徑參數） | 否 |
| [`cwa-tw`](./modules/cwa-tw.md) | 中央氣象署天氣與地震 | 3 | `AUTH_KEY` | 是 |
| [`tw-stock`](./modules/tw-stock/index.md) | 台灣證交所開放資料 | 28（+5 prompt） | 否 | 是 |
| [`gmail`](./modules/gmail.md) | Gmail 郵件收發 | 7 | `GMAIL_CREDENTIALS_FILE_PATH` | 是 |
| [`google-drive`](./modules/google-drive/index.md) | Google Drive 檔案管理 | 22 | `CREDENTIALS_FILE_PATH` | 是 |
| [`google-map`](./modules/google-map.md) | Google Places API | 6 | `GOOGLE_MAPS_API_KEY` | 是 |

## 從哪裡開始

- 第一次接觸這個專案 → [快速開始](./getting-started.md)
- 想理解模組共通的設計 → [架構](./architecture.md)
- 要新增工具或模組 → [開發指南](./development.md)

## 技術棧

| 項目 | 版本 |
|---|---|
| Kotlin | 2.4.0 |
| Java（source/target） | 21 |
| Quarkus | 3.37.3 |
| `quarkus-mcp-server-stdio` | 1.13.1 |
| Gradle wrapper | 9.6.1 |
| 打包格式 | uber-jar |
