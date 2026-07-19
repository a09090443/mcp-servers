---
paths:
  - "cwa-tw/**"
---

# cwa-tw 模組規則

中央氣象署（CWA）開放資料：天氣預報與地震觀測。

## 設定

- 環境變數 `AUTH_KEY`：CWA opendata 授權碼，缺少時 `Weather` 初始化即失敗
- REST client base URI 寫死在 `WeatherClient.kt` 的 `@RegisterRestClient(baseUri = "https://opendata.cwa.gov.tw/api")`

## TLS 設定不可誤刪 `%prod.` 覆寫

`application.properties` 在預設 profile 下放寬 TLS 驗證以便本機開發：

```properties
quarkus.tls.trust-all=true
quarkus.rest-client.ssl.hostname-verification-policy=none

%prod.quarkus.tls.trust-all=false
%prod.quarkus.rest-client.ssl.hostname-verification-policy=match
```

`%prod.` 那組是唯一讓正式環境恢復憑證驗證的機制。調整 TLS 相關設定時務必同步保留，否則正式環境會在無驗證狀態下對外連線。

## API 呼叫限制

CWA 端點有伺服器端的時間區間限制，違反時回傳錯誤而非截斷結果：

- 天氣預報：`timeFrom` 必須早於 `timeTo`，且區間 ≤ 24 小時
- 地震觀測：區間 ≤ 36 小時，且查詢時間不得晚於當下

新增工具或調整預設值時要把這些限制寫進 `@ToolArg` 的 description，讓呼叫端能自行避開。

## 序列化

此模組用 Jackson `ObjectMapper`，**不是** Gson（其他模組多數用 Gson）。工具直接回傳 Kotlin 型別，不手動包 JSON 字串。

## 測試

`WeatherTest` 是 `@QuarkusTest` + `@Inject` 真實 bean，會打線上 API，需要 `AUTH_KEY` 且需連網。城市代號對照（如 `臺北市` → `F-D0047-061`）是硬編斷言，若 CWA 調整代號會失敗。
