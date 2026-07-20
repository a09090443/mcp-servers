---
id: index
title: google-drive
sidebar_label: 概覽
---

# google-drive

Google Drive 檔案與資料夾管理，共 **22 個工具**（全 repo 第二多）。

## 目錄結構

```
google-drive/
├── build.gradle.kts                        # 相依：google-api-client、google-oauth-client-jetty、
│                                           #       google-api-services-drive、commons-io
├── README.md
└── src/
    ├── main/
    │   ├── docker/                         # Dockerfile.jvm / .legacy-jar / .native / .native-micro
    │   ├── kotlin/tw/zipe/mcp/googledrive/
    │   │   ├── GoogleDriveFileOperations.kt    # @Tool 進入點
    │   │   └── SSLUtil.kt                      # TLS 信任設定輔助
    │   └── resources/
    │       └── application.properties      # uber-jar；日誌 → D:/tmp/google-drive.log
    └── test/kotlin/tw/zipe/mcp/googledrive/
        └── GoogleDriveFileOperationsTest.kt    # 需真實憑證（CREDENTIALS_FILE_PATH）
```

## 本節導覽

- [工具](./tools.md) —— 22 個 `@Tool` 的完整清單
- [設定](./setup.md) —— Google Cloud 前置條件、環境變數、OAuth scope
- [實作慣例](./conventions.md) —— 擴充函式風格與 `SSLUtil` 注意事項
- [建置與測試](./development.md) —— 建置指令、MCP 設定、測試風險
