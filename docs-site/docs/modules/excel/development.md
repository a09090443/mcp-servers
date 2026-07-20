---
id: development
title: 建置與測試
sidebar_label: 建置與測試
---

# 建置與測試

## 建置與執行

```bash
./gradlew :excel:build
java -jar excel/build/excel-1.0-SNAPSHOT-runner.jar
```

## MCP 設定

```json
{
  "mcpServers": {
    "excel": {
      "command": "java",
      "args": ["-jar", "path/to/excel-1.0-SNAPSHOT-runner.jar"]
    }
  }
}
```

## 測試

`ExcelFileOperationsTest` 為純本機測試，**可離線執行**，不需憑證或網路。測試會產生暫存 xlsx 檔案，注意清理。

```bash
./gradlew :excel:test
```

與 `date`、`filesystem` 同屬可離線執行的三個模組之一，適合用來驗證建置流程是否正常。
