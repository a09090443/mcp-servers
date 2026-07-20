---
id: development
title: 開發指南
sidebar_position: 5
---

# 開發指南

## 常用指令

一律在**根目錄**執行，用 `:模組名:任務` 指定對象：

```bash
./gradlew :tw-stock:build        # 建置單一模組
./gradlew :tw-stock:quarkusDev   # 開發模式（live coding）
./gradlew :tw-stock:test         # 執行單一模組的測試

./gradlew build                  # 建置全部八個模組
./gradlew projects               # 列出已掛載的模組

# 執行單一測試類別
./gradlew :tw-stock:test --tests "tw.zipe.mcp.twse.tool.TWStockToolTest"

# 執行單一測試方法
./gradlew :tw-stock:test --tests "tw.zipe.mcp.twse.tool.TWStockToolTest.testGetCompanyBasicInfo"

# 建置 native executable（需 GraalVM，或用容器）
./gradlew :tw-stock:build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true
```

此 repo 沒有設定 linter 或 formatter，也沒有 CI 設定檔。

## 新增模組

三個步驟：

1. 在根 `settings.gradle.kts` 的 `include` 加入模組名
2. 建立該模組的 `build.gradle.kts`（照[架構](./architecture.md#共用建置慣例buildsrc)的樣板）
3. 需要的新套件加進 `gradle/libs.versions.toml`

:::danger 不要在模組底下放這些
`settings.gradle.kts`、`gradle.properties`、gradle wrapper —— 全 repo 只該有根目錄那一份。
:::

## 測試現況

:::info repo 內沒有任何單元測試會 mock 外部依賴
`mockito-core` 雖列在 `tw-stock` 的依賴中，但實際上沒有被使用。
:::

跑測試前要分清楚三類：

| 類別 | 模組 | 說明 |
|---|---|---|
| 可離線執行 | `date`、`excel`、`filesystem` | 純本機邏輯 |
| 需要網路、不需憑證 | `tw-stock`、`cwa-tw` | `cwa-tw` 另需 `AUTH_KEY` |
| 需要真實憑證 | `gmail`、`google-drive`、`google-map` | 檔名帶 `IntegrationTest` |

跑全專案的 `./gradlew build` 時，`cwa-tw`、`gmail`、`google-drive`、`google-map` 的測試會因缺少憑證而失敗。要驗證全部模組是否編譯得過：

```bash
./gradlew build -x test
./gradlew :date:test :excel:test :filesystem:test :tw-stock:test
```

### 防禦式斷言

`cwa-tw` 與 `tw-stock` 的測試是 `@QuarkusTest` + `@Inject` 真實 bean，直接打線上 API。因為線上資料會變動，斷言刻意寫得寬鬆：

```kotlin
if (result.isNotEmpty()) { ... }              // 有資料才驗
assertTrue(result.isEmpty() || result.all { ... })
```

新增測試時沿用這種寫法，否則會因為市場休市、資料尚未更新等原因隨機失敗。

所有測試都沒有 `@EnabledIfEnvironmentVariable` 之類的守衛，缺憑證或斷網時會直接失敗而非跳過。

## Commit 訊息規範

格式為 `<類型>(<範圍>): <主旨>`，**範圍請填模組目錄名**：

```
修復(tw-stock): 修正代號過濾在空值時回傳全市場資料
新增(gmail): 新增郵件標籤管理工具
設定: 升級 Quarkus 至 3.37.3
```

類型可用：`新增`、`修復`、`重構`、`測試`、`文件`、`設定`、`效能`、`移除`。跨模組或根層級變更可省略範圍。

## 本機環境注意事項

此 repo 的目錄擁有者 SID 與目前 Windows 帳號不同，git 原本會判定為 dubious ownership 而拒絕執行。已加入全域例外：

```bash
git config --global --add safe.directory D:/projects/mcp-servers
```

若換機器或重設 git 設定後又出現同樣錯誤，重跑上面那行即可。

## `bin/` 目錄

`bin/` 是 Eclipse/VS Code 的編譯輸出，內含 `src/` 的過期副本，已被 gitignore。**絕對不要編輯或引用 `bin/` 底下的檔案**——搜尋時若命中它們，請改看 `src/`。
