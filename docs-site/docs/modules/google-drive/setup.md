---
id: setup
title: 設定
sidebar_label: 設定
---

# 設定

## 前置條件

1. [建立 Google Cloud 專案](https://console.cloud.google.com/projectcreate)
2. [啟用 Google Drive API](https://console.cloud.google.com/apis/library/drive.googleapis.com)
3. [設定 OAuth 同意畫面](https://console.cloud.google.com/apis/credentials/consent)
4. [建立 OAuth 用戶端 ID](https://console.cloud.google.com/apis/credentials)（應用類型選「桌面應用程式」）
5. 下載 OAuth 用戶端憑證 JSON 檔案

## 環境變數

| 變數 | 說明 |
|---|---|
| `CREDENTIALS_FILE_PATH` | OAuth client secret JSON 的路徑 |

:::warning 命名不一致
**沒有 `GOOGLE_` 或 `DRIVE_` 前綴**，與 [`gmail`](../gmail.md) 模組的 `GMAIL_CREDENTIALS_FILE_PATH` 不一致，兩者同時設定時容易搞混。
:::

OAuth token 存於 `tokens/`（常數 `TOKENS_DIRECTORY_PATH`），相對於行程工作目錄建立，已列入 gitignore。

## 需要的 OAuth scope

```
https://www.googleapis.com/auth/drive.file
https://www.googleapis.com/auth/drive.metadata.readonly
```
