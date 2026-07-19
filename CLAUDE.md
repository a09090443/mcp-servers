# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 專案性質

這個 repo 收納 8 個**彼此獨立**的 MCP（Model Context Protocol）Server。

**關鍵：這不是 Gradle multi-module 專案。** 根目錄沒有 `build.gradle.kts` 或 `settings.gradle.kts`，每個模組各自帶著完整的 `settings.gradle.kts`、`gradle.properties` 與 gradle wrapper。任何 Gradle 指令都必須先 `cd` 進模組目錄，在根目錄執行一律失敗。

## 目錄樹

```
mcp-servers/
├── .gitignore                    # 根層級 IDE／建置產物忽略規則
├── README.md                     # 各模組功能總覽
├── cwa-tw/                       # 中央氣象署：天氣預報與地震觀測
│   └── src/main/kotlin/tw/zipe/mcp/cwa/
│       ├── Weather.kt            # @Tool 進入點
│       └── WeatherClient.kt      # @RegisterRestClient → opendata.cwa.gov.tw/api
├── date/                         # 日期與時區
│   └── src/main/kotlin/tw/zipe/mcp/date/
│       └── DateZoneOperations.kt
├── excel/                        # Excel 檔案操作（Apache POI）
│   └── src/main/kotlin/tw/zipe/mcp/excel/
│       └── ExcelFileOperations.kt
├── filesystem/                   # 本機檔案系統
│   └── src/main/kotlin/tw/zipe/mcp/filesystem/
│       ├── FileSystemApplication.kt   # @QuarkusMain（僅此模組有）
│       └── FileSystemOperations.kt
├── gmail/                        # Gmail 郵件收發
│   └── src/main/kotlin/tw/zipe/mcp/gmail/
│       └── GmailOperations.kt
├── google-drive/                 # Google Drive 檔案管理
│   └── src/main/kotlin/tw/zipe/mcp/googledrive/
│       ├── GoogleDriveFileOperations.kt
│       └── SSLUtil.kt
├── google-map/                   # Google Places API
│   └── src/main/kotlin/tw/zipe/mcp/googlemap/places/
│       ├── GoogleMapsPlacesOperations.kt
│       └── RemoveTrailingUnderscoreNamingStrategy.kt
└── tw-stock/                     # 台灣證交所 TWSE 開放資料（規模最大）
    ├── API.md                    # TWSE 端點對照
    └── src/main/kotlin/tw/zipe/mcp/twse/
        ├── TWStockClient.kt      # @RegisterRestClient → openapi.twse.com.tw/v1
        ├── tool/TWStockTool.kt   # 30 個 @Tool（類別名為 TWStock，與檔名不同）
        ├── prompt/TWStockPrompt.kt   # 5 個 @Prompt（全 repo 唯一）
        └── enumerate/IndustryCategory.kt
```

每個模組底下另有 `src/main/resources/application.properties`、`src/test/kotlin/...`、`README.md`、`.gitignore`、`.dockerignore`。

**`bin/` 目錄是 Eclipse/VS Code 的編譯輸出**，內含 `src/` 的過期副本，已被 gitignore。絕對不要編輯或引用 `bin/` 底下的檔案——搜尋時若命中它們，請改看 `src/`。

## 共用技術棧

所有模組一致，無例外：

| 項目 | 版本 |
|---|---|
| Kotlin | 2.2.0 |
| Java（source/target） | 21 |
| Quarkus | 3.20.2.1 |
| `quarkus-mcp-server-stdio` | 1.4.1 |
| 打包 | `uber-jar` |

## 常用指令

一律先進入模組目錄：

```bash
cd tw-stock            # 或其他模組

./gradlew build        # 建置（產出 build/*-runner.jar，uber-jar）
./gradlew quarkusDev   # 開發模式（live coding）
./gradlew test         # 執行測試

# 執行單一測試類別
./gradlew test --tests "tw.zipe.mcp.twse.tool.TWStockToolTest"

# 執行單一測試方法
./gradlew test --tests "tw.zipe.mcp.twse.tool.TWStockToolTest.testGetCompanyBasicInfo"

# 建置 native executable（需 GraalVM，或用容器）
./gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true
```

執行打包後的 server：

```bash
java -jar build/tw-stock-1.0-SNAPSHOT-runner.jar
```

此 repo 沒有設定 linter 或 formatter，也沒有 CI 設定檔。

## 架構要點

### STDIO transport：stdout 是協議通道

所有 server 走 `quarkus-mcp-server-stdio`，**stdout 專屬於 MCP 協議**。任何寫入 stdout 的內容都會破壞協議。因此所有模組的 `application.properties` 都把日誌導向檔案：

```properties
quarkus.log.file.enable=true
quarkus.log.file.path=D:/tmp/<module>.log
```

新增程式碼時不要用 `println`／`System.out`；要輸出診斷訊息請用 Quarkus 的 `Log` 或 `java.util.logging`。（註：上述日誌路徑寫死為 Windows 的 `D:/tmp/`，換平台需調整。）

### CDI 與 allOpen

工具類別標註 `@ApplicationScoped`，Kotlin 類別預設 final，所以每個 `build.gradle.kts` 都設定了 allOpen plugin：

```kotlin
allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}
```

新增的 CDI bean 若用了清單外的註解，需一併加入 allOpen，否則注入會失敗。另外 `javaParameters = true` 是必要設定——MCP 靠參數名產生 tool schema。

### 兩種回傳慣例（新增工具時請對齊所在模組）

**A. 回傳 JSON 字串**（`date`、`excel`、`filesystem`、`gmail`、`google-drive`、`google-map`）
用 Gson 序列化，統一包成 `{success, data}` / `{error, message, data}`。內部有兩種寫法：

- `createSuccessResponse()` / `createErrorResponse()` 私有函式 —— `date`、`excel`、`filesystem`、`gmail`
- `Any.toSuccessResponse()` 擴充函式 + `executeWithErrorHandling { }` inline 包裝 —— `google-drive`、`google-map`

**B. 直接回傳 Kotlin 型別**（`tw-stock`、`cwa-tw`）
回傳 `List<Map<String, Any>>` 或 `Map<String, Any>`，由框架序列化。`cwa-tw` 用 Jackson `ObjectMapper` 而非 Gson。

### 設定一律透過環境變數

沒有任何模組把金鑰寫進 `application.properties`；全部在執行時讀 `System.getenv()`，缺少時直接拋例外。

## 各模組規則

### `cwa-tw`
- 環境變數：`AUTH_KEY`（中央氣象署 opendata 授權碼）
- REST client base URI 寫死在 `@RegisterRestClient` 註解中
- `application.properties` 在非 prod profile 下開啟 `trust-all=true` 繞過 TLS 驗證；`%prod.` 前綴的設定會關閉它。**修改 TLS 設定時務必保留 `%prod.` 覆寫。**
- API 有時間區間限制：天氣預報 ≤ 24 小時、地震觀測 ≤ 36 小時，且地震查詢時間不得晚於當下

### `date`
- 無外部依賴、無環境變數，是唯一可離線完整測試的模組
- 新增日期功能時優先改這裡

### `excel`
- 基於 Apache POI，操作的是本機檔案路徑
- 注意 `ExcelFileOperations.kt` 的 companion object 縮排與其他模組不同（多縮一層），是既有格式問題，不必特意對齊

### `filesystem`
- **唯一有 `@QuarkusMain` 進入點的模組。** `FileSystemApplication` 從 CLI 參數接收允許存取的路徑清單，寫入 `fileserver.paths` system property；未提供參數則以 exit code 1 終止
- 因此啟動方式與其他模組不同：`java -jar build/filesystem-*-runner.jar <允許路徑1> <允許路徑2>`
- 修改路徑限制邏輯時記得同步 `FileSystemApplication` 與 `FileSystemOperations`

### `gmail`
- 環境變數：`GMAIL_CREDENTIALS_FILE_PATH`（OAuth client secret JSON 路徑）
- OAuth token 存於 `gmail_tokens/`（相對於行程工作目錄，已 gitignore）
- 需要的 scope：`gmail_send`、`gmail_readonly`、`gmail_modify`
- 刪除郵件是移至垃圾桶

### `google-drive`
- 環境變數：`CREDENTIALS_FILE_PATH`（注意**沒有** `GOOGLE_` 前綴，與 gmail 的變數名不同）
- OAuth token 存於 `tokens/`（已 gitignore）
- `SSLUtil.kt` 提供 SSL 設定輔助，動它之前先確認是否影響憑證驗證

### `google-map`
- 環境變數：`GOOGLE_MAPS_API_KEY`
- **每次呼叫都會用 `createPlacesClientWithFieldMask()` 建立新的 `PlacesClient`**，以便帶入該次請求專屬的 `x-goog-fieldmask` header。Places API 強制要求 field mask，且欄位前綴規則不一致：`getPlaceDetails` 不加前綴，其餘方法需要 `places.` 前綴——這個分支邏輯在 `addPlacesPrefix()`
- 新增工具時務必經由這個路徑建立 client，直接用注入的 `placesClient` 會因缺少 field mask 而失敗
- `RemoveTrailingUnderscoreNamingStrategy` 用於處理 Google API 產生的欄位尾底線

### `tw-stock`
- 無環境變數，TWSE 開放資料不需金鑰
- **TWSE API 一律回傳全市場資料，沒有依股票代號查詢的端點。** 過濾在 client 端做：取回整份清單後以 `公司代號` 這個中文 key 比對（常數 `CODE_KEY`）。既有慣例是代號為空白時回傳完整清單：
  ```kotlin
  data.filter { it[CODE_KEY] == code }.ifEmpty {
      if (!code.isNullOrBlank()) emptyList() else data
  }
  ```
  注意不同端點的代號欄位名稱未必都是 `公司代號`，新增工具前先確認實際回傳結構
- 唯一提供 `@Prompt` 的模組，prompt 內容為繁體中文投資分析範本
- `tool/TWStockTool.kt` 內的類別名為 `TWStock`，與檔名不一致
- `getCompanyBusinessData` 已被註解停用，`TWStockClient` 中對應方法仍在

## 測試現況

**repo 內沒有任何單元測試會 mock 外部依賴。** `mockito-core` 雖列在 `tw-stock` 的依賴中，但實際上沒有被使用。跑測試前要分清楚三類：

- **可離線執行**：`date`、`excel`、`filesystem`（純本機邏輯）
- **需要網路、不需憑證**：`tw-stock`、`cwa-tw`（`cwa-tw` 另需 `AUTH_KEY`）
- **需要真實憑證**：`gmail`、`google-drive`、`google-map`（檔名帶 `IntegrationTest`）

`cwa-tw` 與 `tw-stock` 的測試是 `@QuarkusTest` + `@Inject` 真實 bean，直接打線上 API。因為線上資料會變動，`TWStockToolTest` 的斷言寫得很寬鬆：

```kotlin
if (result.isNotEmpty()) { ... }              // 有資料才驗
assertTrue(result.isEmpty() || result.all { ... })
```

新增測試時沿用這種防禦式寫法，否則會因為市場休市、資料尚未更新等原因隨機失敗。

所有測試都沒有 `@EnabledIfEnvironmentVariable` 之類的守衛，缺憑證或斷網時會直接失敗而非跳過。`GmailOperationsIntegrationTest` 用 `@TestMethodOrder` 串接情境（寄信 → 搜尋 → 標記 → 刪除），且 `testEmailAddress` 硬編為 `test@example.com`，實際執行前需修改。

## 本機環境注意事項

此 repo 在目前機器上有 git ownership 不符的問題，所有 git 指令需要加旗標：

```bash
git -c safe.directory=D:/projects/mcp-servers <command>
```

或請使用者一次性設定 `git config --global --add safe.directory D:/projects/mcp-servers`。

Commit 訊息遵循使用者全域 CLAUDE.md 的規範；**範圍（scope）請填模組目錄名**，例如 `修復(tw-stock): ...`、`新增(gmail): ...`。跨模組或根層級變更可省略範圍。
