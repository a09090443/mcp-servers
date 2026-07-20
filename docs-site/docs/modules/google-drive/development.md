---
id: development
title: 建置與測試
sidebar_label: 建置與測試
---

# 建置與測試

## 建置與執行

```bash
./gradlew :google-drive:build
java -jar google-drive/build/google-drive-1.0-SNAPSHOT-runner.jar
```

或用 JBang：`jbang --quiet google-drive-1.0-SNAPSHOT-runner.jar`

## MCP 設定

```json
{
  "mcpServers": {
    "gdrive": {
      "command": "java",
      "args": ["-jar", "D:\\MCP\\google-drive-1.0-SNAPSHOT-runner.jar"],
      "env": {
        "CREDENTIALS_FILE_PATH": "${CREDENTIALS_FILE_PATH}"
      }
    }
  }
}
```

## 測試會操作真實 Drive

`GoogleDriveFileOperationsTest` 需要真實憑證且會操作實際的 Drive 內容（**包含建立與刪除**）。執行前確認用的是測試帳號，不要對正式資料跑。

```bash
./gradlew :google-drive:test    # 需先設定 CREDENTIALS_FILE_PATH
```

沒有 `@EnabledIfEnvironmentVariable` 之類的守衛，缺憑證時會直接失敗而非跳過。
