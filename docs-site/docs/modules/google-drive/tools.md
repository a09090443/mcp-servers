---
id: tools
title: 工具
sidebar_label: 工具
---

# 工具

共 22 個 `@Tool`，全部定義在 `GoogleDriveFileOperations.kt`。

:::tip 工具數量已多，先查再加
22 個工具已涵蓋檔案 CRUD、上傳下載、目錄管理、搜尋、依檔名查 ID、存在性檢查。新增功能前先確認是否已有近似工具，避免產生語意重疊的 API 讓呼叫端難以選擇。
:::

## 檔案操作

| 工具 | 說明 |
|---|---|
| `createNewFile` | 在 Drive 建立新檔案 |
| `uploadFile` | 將本機檔案上傳到 Drive |
| `uploadFileToDirectory` | 上傳到指定目錄 |
| `readFile` | 讀取檔案內容 |
| `downloadFile` | 下載檔案 |
| `updateFile` | 更新檔案內容 |
| `updateFileFromLocal` | 以本機檔案更新 Drive 檔案 |
| `renameFile` | 更改檔案名稱 |
| `moveFile` | 將檔案移動到指定目錄 |
| `deleteFile` | 刪除檔案 |

## 查詢

| 工具 | 說明 |
|---|---|
| `listFiles` | 獲取檔案列表 |
| `getFileById` | 依 ID 取得檔案 |
| `getFileIdByName` | 依檔名查詢檔案 ID |
| `searchFiles` | 關鍵字模糊搜尋 |
| `searchFilesInDirectory` | 在指定目錄內搜尋檔案 |
| `searchDirectories` | 搜尋目錄 |
| `checkFileExistsInDirectory` | 檢查指定目錄下檔案是否存在 |

## 目錄操作

| 工具 | 說明 |
|---|---|
| `createDirectory` | 在根目錄建立目錄 |
| `createDirectoryInParent` | 在指定父目錄下建立目錄 |
| `listDirectoryContents` | 顯示目錄中的檔案和子目錄 |
| `renameDirectory` | 更改目錄名稱 |
| `deleteDirectory` | 刪除目錄 |
