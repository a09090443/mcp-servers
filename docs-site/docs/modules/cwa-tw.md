---
id: cwa-tw
title: cwa-tw
sidebar_label: cwa-tw
---

# cwa-tw

中央氣象署（CWA）開放資料：天氣預報與地震觀測，共 **3 個工具**。

## 目錄結構

```
cwa-tw/
├── build.gradle.kts                        # 相依：quarkus-rest-client(+jackson)、gson、microprofile-rest-client-api
├── README.md
└── src/
    ├── main/
    │   ├── docker/                         # Dockerfile.jvm / .legacy-jar / .native / .native-micro
    │   ├── kotlin/tw/zipe/mcp/cwa/
    │   │   ├── Weather.kt                  # @Tool 進入點
    │   │   └── WeatherClient.kt            # @RegisterRestClient → opendata.cwa.gov.tw/api
    │   └── resources/
    │       └── application.properties      # 日誌 → D:/tmp/cwa-tw.log；TLS 設定含 %prod. 覆寫
    └── test/kotlin/tw/zipe/mcp/cwa/
        └── WeatherTest.kt                  # @QuarkusTest，需網路與 AUTH_KEY
```

## 工具

| 工具 | 說明 |
|---|---|
| `getCityWeatherForecastApiId` | 獲取對應台灣各縣市的 API 代號 |
| `getTownshipWeatherForecast` | 獲取全台鄉鎮天氣預報（降雨機率、溫度、體感溫度等） |
| `getEarthquakeData` | 獲取指定區域的地震觀測資料 |

## 設定

### 前置條件

[取得 CWA 開放資料授權碼](https://opendata.cwa.gov.tw/user/authkey)

### 環境變數

| 變數 | 說明 |
|---|---|
| `AUTH_KEY` | CWA opendata 授權碼，缺少時 `Weather` 初始化即失敗 |

REST client base URI 寫死在 `WeatherClient.kt` 的 `@RegisterRestClient(baseUri = "https://opendata.cwa.gov.tw/api")`。

## 建置與執行

```bash
./gradlew :cwa-tw:build
java -jar cwa-tw/build/cwa-tw-1.0-SNAPSHOT-runner.jar
```

## MCP 設定

```json
{
  "mcpServers": {
    "cwa": {
      "command": "java",
      "args": ["-jar", "D:\\MCP\\cwa-tw-1.0-SNAPSHOT-runner.jar"],
      "env": {
        "AUTH_KEY": "${AUTH_KEY}"
      }
    }
  }
}
```

## API 呼叫限制

CWA 端點有伺服器端的時間區間限制，違反時回傳錯誤而非截斷結果：

| 端點 | 限制 |
|---|---|
| 天氣預報 | `timeFrom` 必須早於 `timeTo`，且區間 ≤ 24 小時 |
| 地震觀測 | 區間 ≤ 36 小時，且查詢時間不得晚於當下 |

時間格式統一為 `yyyy-MM-ddThh:mm:ss`。新增工具或調整預設值時要把這些限制寫進 `@ToolArg` 的 description，讓呼叫端能自行避開。

## TLS 設定不可誤刪 `%prod.` 覆寫

`application.properties` 在預設 profile 下放寬 TLS 驗證以便本機開發：

```properties
quarkus.tls.trust-all=true
quarkus.rest-client.ssl.hostname-verification-policy=none

%prod.quarkus.tls.trust-all=false
%prod.quarkus.rest-client.ssl.hostname-verification-policy=match
```

:::danger
`%prod.` 那組是唯一讓正式環境恢復憑證驗證的機制。調整 TLS 相關設定時務必同步保留，否則正式環境會在無驗證狀態下對外連線。
:::

## 實作慣例

**序列化**：此模組用 Jackson `ObjectMapper`，**不是** Gson（其他模組多數用 Gson）。工具直接回傳 Kotlin 型別，不手動包 JSON 字串。

## 測試

`WeatherTest` 是 `@QuarkusTest` + `@Inject` 真實 bean，會打線上 API，需要 `AUTH_KEY` 且需連網。

城市代號對照（如 `臺北市` → `F-D0047-061`）是硬編斷言，若 CWA 調整代號會失敗。
