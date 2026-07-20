---
id: conventions
title: 實作慣例
sidebar_label: 實作慣例
---

# 實作慣例

## 擴充函式風格

此模組用**擴充函式**風格，與 `date`／`excel`／`filesystem`／`gmail` 的 `createSuccessResponse()` 函式風格不同：

```kotlin
private fun Map<String, Any?>.toSuccessResponse(): String
```

搭配統一的錯誤處理包裝。新增工具時沿用擴充函式風格，不要混入另一種寫法。

同屬擴充函式派的還有 `google-map`；兩者可互相參考。

## SSLUtil.kt

此檔案提供 SSL/TLS 設定輔助。修改前先確認是否會關閉憑證驗證——放寬 TLS 會影響所有對 Google API 的連線。若只是為了排除本機連線問題，優先找其他解法。
