# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 專案性質

這個 repo 收納 8 個 MCP（Model Context Protocol）Server，以 **Gradle multi-module** 組織：根目錄的 `settings.gradle.kts` 用 `include` 掛載八個子專案，共用單一 gradle wrapper 與單一 version catalog。

**Gradle 指令一律在根目錄執行**，以 `:模組名:任務` 指定對象（例如 `./gradlew :tw-stock:build`）。子模組底下已經沒有 `gradlew`、`settings.gradle.kts` 或 `gradle.properties`，`cd` 進模組目錄是跑不動的。

各模組在**執行期**仍彼此獨立：沒有任何跨模組的 `project(":...")` 相依，每個都各自打包成可獨立部署的 uber-jar。共用的只有建置設定與套件版本。

## 目錄樹

```
mcp-servers/
├── .gitignore                    # 根層級 IDE／建置產物忽略規則
├── README.md                     # 各模組功能總覽
├── settings.gradle.kts           # include 八個子模組（全 repo 唯一）
├── gradlew / gradlew.bat         # 唯一的 gradle wrapper
├── gradle/
│   ├── libs.versions.toml        # 全 repo 唯一的版本來源（version catalog）
│   └── wrapper/
├── buildSrc/                     # 共用建置慣例
│   └── src/main/kotlin/
│       └── mcp-server.conventions.gradle.kts   # 八個模組共用的建置設定
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
| Kotlin | 2.4.0 |
| Java（source/target） | 21 |
| Quarkus | 3.37.3 |
| `quarkus-mcp-server-stdio` | 1.13.1 |
| Gradle wrapper | 9.6.1 |
| 打包 | `uber-jar` |

## 版本集中管理

所有套件版本集中在根目錄的 `gradle/libs.versions.toml`。這是 Gradle 的預設路徑，**會自動註冊為所有子模組都能用的 `libs`**，不需要任何引用設定。

子模組的 `build.gradle.kts` 一律使用 catalog 別名，**不要寫死版本號**。

## 共用建置慣例（buildSrc）

八個模組共用的建置設定收在 `buildSrc/src/main/kotlin/mcp-server.conventions.gradle.kts`，包含：套用 kotlin/allopen/quarkus 三個插件、repositories、Java 21、`javaParameters`、allOpen 註解清單、測試的 logmanager 設定、UTF-8 編碼、`version`，以及每個模組都要的相依（quarkus-bom、quarkus-kotlin、kotlin-stdlib、quarkus-arc、mcp-server-stdio、quarkus-junit5）。

因此模組的 `build.gradle.kts` 只剩三件事——套慣例插件、宣告專屬相依、指定 `group`：

```kotlin
plugins {
    id("mcp-server.conventions")
}

dependencies {
    implementation(libs.gson)
    testImplementation(libs.mockito.core)
}

group = "tw.zipe.mcp.date"
```

**改共用設定就改 conventions 檔，不要在個別模組重複宣告。** 模組端若再寫一次 `kotlin { }` 或 `allOpen { }` 會覆蓋慣例、造成模組間行為分歧，這正是先前 Kotlin 版本漂移的成因。

`buildSrc` 是獨立建置，不會自動繼承根專案的 catalog，因此 `buildSrc/settings.gradle.kts` 有一段明確的 `from(files("../gradle/libs.versions.toml"))`。另外 `buildSrc/build.gradle.kts` 裡那行 `implementation(files(libs.javaClass...codeSource.location))` 是讓慣例插件能使用型別安全 `libs` 存取子的必要 workaround（Gradle 尚未原生支援），**移除會導致 conventions 檔編譯失敗**。

新增模組時要做三件事：在根 `settings.gradle.kts` 的 `include` 加入模組名、建立該模組的 `build.gradle.kts`（照上面的樣板）、需要的新套件加進 catalog。**不要**在模組底下放 `settings.gradle.kts`、`gradle.properties` 或 gradle wrapper。

### 改版本時的兩個陷阱

**一、Kotlin 必須對齊 quarkus-bom。** 模組用的是 `enforcedPlatform`（強制覆寫，不是一般 `platform`），BOM 管理的版本會**強制蓋掉**自行宣告的版本。Quarkus 3.37.3 管理 Kotlin 2.4.0，所以 catalog 的 `kotlin` 就設 2.4.0；若設成更新的 2.4.10，會變成新編譯器搭配被降版的 stdlib。

**二、被 BOM 管理的套件不能自行升上去。** 例如 `google-http-client` 已有 2.x，但 quarkus-bom 也管理它，宣告 2.1.1 會被強制降回 1.47.1。catalog 中直接寫實際生效的 1.47.1，避免宣告與解析結果不符。

升級後請用以下指令確認宣告值等於實際解析值：

```bash
./gradlew dependencies --configuration runtimeClasspath | grep '套件名'
# 出現 "2.1.1 -> 1.47.1" 這種箭頭，代表宣告被 BOM 覆寫了
```

## 常用指令

一律在**根目錄**執行，用 `:模組名:任務` 指定對象：

```bash
./gradlew :tw-stock:build        # 建置單一模組（產出 tw-stock/build/*-runner.jar）
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

跑全專案的 `./gradlew build` 時，`cwa-tw`、`gmail`、`google-drive`、`google-map` 的測試會因缺少憑證而失敗。要驗證全部模組是否編譯得過，用 `./gradlew build -x test`，再單獨跑可離線／免憑證的四個：

```bash
./gradlew build -x test
./gradlew :date:test :excel:test :filesystem:test :tw-stock:test
```

執行打包後的 server：

```bash
java -jar tw-stock/build/tw-stock-1.0-SNAPSHOT-runner.jar
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

工具類別標註 `@ApplicationScoped`，Kotlin 類別預設 final，所以慣例插件（`buildSrc/src/main/kotlin/mcp-server.conventions.gradle.kts`）統一設定了 allOpen plugin：

```kotlin
allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}
```

新增的 CDI bean 若用了清單外的註解，需在**慣例插件**裡一併加入 allOpen（不要加在個別模組），否則注入會失敗。另外 `javaParameters = true` 是必要設定——MCP 靠參數名產生 tool schema，缺了它 `@Tool` 的參數名會變成 `arg0`、`arg1`。

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

各模組的專屬規則（環境變數、進入點、既有陷阱、測試方式）拆在 `.claude/rules/` 底下，以 `paths` frontmatter 限定範圍，**只有在讀取該模組檔案時才會載入**：

| 規則檔 | 涵蓋範圍 | 關鍵內容 |
|---|---|---|
| `.claude/rules/cwa-tw.md` | `cwa-tw/**` | `AUTH_KEY`、TLS `%prod.` 覆寫、API 時間區間限制 |
| `.claude/rules/date.md` | `date/**` | 無依賴、可離線測試 |
| `.claude/rules/excel.md` | `excel/**` | Apache POI 資源關閉、既有縮排差異 |
| `.claude/rules/filesystem.md` | `filesystem/**` | `@QuarkusMain`、CLI 路徑參數為存取控制機制 |
| `.claude/rules/gmail.md` | `gmail/**` | `GMAIL_CREDENTIALS_FILE_PATH`、OAuth scope、測試會真的寄信 |
| `.claude/rules/google-drive.md` | `google-drive/**` | `CREDENTIALS_FILE_PATH`（無前綴）、`SSLUtil` |
| `.claude/rules/google-map.md` | `google-map/**` | field mask 必經路徑、前綴規則不一致、測試會計費 |
| `.claude/rules/tw-stock.md` | `tw-stock/**` | client 端過濾、`@Prompt`、測試打線上 API |

要改某個模組時，直接讀該模組的檔案即可觸發對應規則載入；也可以主動開啟對應的 `.claude/rules/*.md` 查閱。新增模組時請一併建立規則檔並更新此表。

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

此 repo 的目錄擁有者 SID 與目前 Windows 帳號不同（多半是由舊帳號建立），git 原本會判定為 dubious ownership 而拒絕執行。**此問題已排除**，已加入全域例外：

```bash
git config --global --add safe.directory D:/projects/mcp-servers
```

git 指令現在可直接執行，不需要再加 `-c safe.directory=...` 旗標。若換機器或重設 git 設定後又出現同樣錯誤，重跑上面那行即可。

Commit 訊息遵循使用者全域 CLAUDE.md 的規範；**範圍（scope）請填模組目錄名**，例如 `修復(tw-stock): ...`、`新增(gmail): ...`。跨模組或根層級變更可省略範圍。
