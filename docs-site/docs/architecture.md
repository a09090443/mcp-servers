---
id: architecture
title: 架構
sidebar_position: 3
---

# 架構

## Gradle multi-module

根目錄的 `settings.gradle.kts` 用 `include` 掛載八個子專案，共用單一 wrapper 與單一 version catalog。

各模組在**執行期**彼此獨立：沒有任何跨模組的 `project(":...")` 相依，每個都各自打包成可獨立部署的 uber-jar。共用的只有建置設定與套件版本。

```
mcp-servers/
├── settings.gradle.kts           # include 八個子模組（全 repo 唯一）
├── gradlew / gradlew.bat         # 唯一的 gradle wrapper
├── gradle/libs.versions.toml     # 全 repo 唯一的版本來源
├── buildSrc/                     # 共用建置慣例
│   └── src/main/kotlin/mcp-server.conventions.gradle.kts
└── <八個模組>/
```

## STDIO transport：stdout 是協議通道

所有 server 走 `quarkus-mcp-server-stdio`，**stdout 專屬於 MCP 協議**。任何寫入 stdout 的內容都會破壞協議。

因此所有模組的 `application.properties` 都把日誌導向檔案：

```properties
quarkus.log.file.enable=true
quarkus.log.file.path=D:/tmp/<module>.log
```

:::danger 不要用 println
新增程式碼時不要用 `println` 或 `System.out`。要輸出診斷訊息請用 Quarkus 的 `Log` 或 `java.util.logging`。
:::

上述日誌路徑寫死為 Windows 的 `D:/tmp/`，換平台需調整。

## 共用建置慣例（buildSrc）

八個模組共用的建置設定收在 `buildSrc/src/main/kotlin/mcp-server.conventions.gradle.kts`，包含：kotlin/allopen/quarkus 三個插件、repositories、Java 21、`javaParameters`、allOpen 註解清單、測試的 logmanager 設定、UTF-8 編碼、`version`，以及每個模組都要的相依。

模組的 `build.gradle.kts` 因此只剩三件事：

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

:::warning 改共用設定就改 conventions 檔
模組端若再寫一次 `kotlin { }` 或 `allOpen { }` 會覆蓋慣例、造成模組間行為分歧——這正是先前 Kotlin 版本漂移的成因。
:::

## CDI 與 allOpen

工具類別標註 `@ApplicationScoped`，而 Kotlin 類別預設 final，所以慣例插件統一設定了 allOpen：

```kotlin
allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}
```

新增的 CDI bean 若用了清單外的註解，需在**慣例插件**裡一併加入（不要加在個別模組），否則注入會失敗。

另外 `javaParameters = true` 是必要設定——MCP 靠參數名產生 tool schema，缺了它 `@Tool` 的參數名會變成 `arg0`、`arg1`。

## 兩種回傳慣例

新增工具時請對齊所在模組的既有寫法。

### A. 回傳 JSON 字串

`date`、`excel`、`filesystem`、`gmail`、`google-drive`、`google-map` 用 Gson 序列化，統一包成 `{success, data}` / `{error, message, data}`。內部又分兩種寫法：

- **私有函式**：`createSuccessResponse()` / `createErrorResponse()` —— `date`、`excel`、`filesystem`、`gmail`
- **擴充函式**：`Any.toSuccessResponse()` + `executeWithErrorHandling { }` inline 包裝 —— `google-drive`、`google-map`

### B. 直接回傳 Kotlin 型別

`tw-stock`、`cwa-tw` 回傳 `List<Map<String, Any>>` 或 `Map<String, Any>`，由框架序列化。`cwa-tw` 用 Jackson `ObjectMapper` 而非 Gson。

## 設定一律透過環境變數

沒有任何模組把金鑰寫進 `application.properties`；全部在執行時讀 `System.getenv()`，缺少時直接拋例外。

## 版本集中管理

所有套件版本集中在根目錄的 `gradle/libs.versions.toml`。這是 Gradle 的預設路徑，會自動註冊為所有子模組都能用的 `libs`，不需要任何引用設定。子模組一律使用 catalog 別名，**不要寫死版本號**。

### 改版本時的兩個陷阱

**一、Kotlin 必須對齊 quarkus-bom。** 模組用的是 `enforcedPlatform`（強制覆寫，不是一般 `platform`），BOM 管理的版本會強制蓋掉自行宣告的版本。Quarkus 3.37.3 管理 Kotlin 2.4.0，所以 catalog 的 `kotlin` 就設 2.4.0；若設成更新的 2.4.10，會變成新編譯器搭配被降版的 stdlib。

**二、被 BOM 管理的套件不能自行升上去。** 例如 `google-http-client` 已有 2.x，但 quarkus-bom 也管理它，宣告 2.1.1 會被強制降回 1.47.1。catalog 中直接寫實際生效的版本，避免宣告與解析結果不符。

升級後請確認宣告值等於實際解析值：

```bash
./gradlew dependencies --configuration runtimeClasspath | grep '套件名'
# 出現 "2.1.1 -> 1.47.1" 這種箭頭，代表宣告被 BOM 覆寫了
```
