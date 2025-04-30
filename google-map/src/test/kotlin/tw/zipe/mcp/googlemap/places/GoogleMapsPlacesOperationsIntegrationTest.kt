package tw.zipe.mcp.googlemap.places

import com.google.gson.JsonParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable

/**
 * @author zipe1
 * @created 2025/4/30
 * GoogleMapsPlacesOperations 的整合測試
 * 注意：這些測試需要 GOOGLE_MAPS_API_KEY 環境變數才能運行
 */
@EnabledIfEnvironmentVariable(named = "GOOGLE_MAPS_API_KEY", matches = ".+")
class GoogleMapsPlacesOperationsIntegrationTest {

    private lateinit var placesOperations: GoogleMapsPlacesOperations

    @BeforeEach
    fun setup() {
        // 直接初始化，使用真實環境變數
        placesOperations = GoogleMapsPlacesOperations()
    }

    @Test
    fun `searchPlaces should return results for valid query`() {
        // 執行
        val result = placesOperations.searchPlaces("台北101")
        val jsonObject = JsonParser.parseString(result).asJsonObject

        // 驗證
        assertTrue(jsonObject.get("success").asBoolean)
        assertNotNull(jsonObject.get("places"))
        val places = jsonObject.getAsJsonArray("places")
        assertFalse(places.isEmpty)

        // 檢查第一個結果
        val firstPlace = places[0].asJsonObject
        assertNotNull(firstPlace.get("name"))
        assertNotNull(firstPlace.get("placeId"))
        assertNotNull(firstPlace.get("address"))
    }

    @Test
    fun `searchPlaces should return results with location parameter`() {
        // 台北市座標
        val latitude = 25.033
        val longitude = 121.565

        // 執行
        val result = placesOperations.searchPlaces(
            query = "咖啡店",
            latitude = latitude,
            longitude = longitude,
            radius = 2000
        )
        val jsonObject = JsonParser.parseString(result).asJsonObject

        // 驗證
        assertTrue(jsonObject.get("success").asBoolean)
        assertNotNull(jsonObject.get("places"))
        val places = jsonObject.getAsJsonArray("places")
        assertFalse(places.isEmpty)
    }

    @Test
    fun `getNearbyPlaces should return places near given location`() {
        // 台北市座標
        val latitude = 25.2593452
        val longitude = 121.5024341

        // 執行
        val result = placesOperations.getNearbyPlaces(
            latitude = latitude,
            longitude = longitude,
            radius = 5000,
            placeType = "RESTAURANT",
            openNow = true,
            language = "zh-TW",
            maxResults = 10
        )
        val jsonObject = JsonParser.parseString(result).asJsonObject

        // 驗證
        assertTrue(jsonObject.get("success").asBoolean)
        assertNotNull(jsonObject.get("places"))
        val places = jsonObject.getAsJsonArray("places")
        assertFalse(places.isEmpty)

        // 檢查返回的位置資訊
        val locationObj = jsonObject.getAsJsonObject("location")
        assertEquals(latitude, locationObj.get("lat").asDouble, 0.001)
        assertEquals(longitude, locationObj.get("lng").asDouble, 0.001)
    }

    @Test
    fun `findPlaceFromText should return results for valid query`() {
        // 執行
        val result = placesOperations.findPlaceFromText("台北101")
        val jsonObject = JsonParser.parseString(result).asJsonObject

        // 驗證
        assertTrue(jsonObject.get("success").asBoolean)
        assertNotNull(jsonObject.get("places"))
        val places = jsonObject.getAsJsonArray("places")
        assertFalse(places.isEmpty)
    }

    @Test
    fun `getPlaceDetails should return details for valid placeId`() {
        // 先獲取一個有效的placeId
        val searchResult = placesOperations.searchPlaces("台北101")
        val searchJson = JsonParser.parseString(searchResult).asJsonObject
        val places = searchJson.getAsJsonArray("places")
        val placeId = places[0].asJsonObject.get("placeId").asString

        // 執行
        val result = placesOperations.getPlaceDetails(placeId)
        val jsonObject = JsonParser.parseString(result).asJsonObject

        // 驗證
        assertTrue(jsonObject.get("success").asBoolean)
        assertEquals(placeId, jsonObject.get("placeId").asString)
        assertNotNull(jsonObject.get("name"))
        assertNotNull(jsonObject.get("formattedAddress"))
    }

    @Test
    fun `getPlacePhotoUrl should generate valid URL`() {
        // 先獲取一個有照片的地點
        val searchResult = placesOperations.searchPlaces("台北101")
        val searchJson = JsonParser.parseString(searchResult).asJsonObject
        val places = searchJson.getAsJsonArray("places")
        val placeId = places[0].asJsonObject.get("placeId").asString

        // 獲取地點詳情
        val detailsResult = placesOperations.getPlaceDetails("ChIJN1t_tDeuQjQRIM2xDV9kFHw")
        val detailsJson = JsonParser.parseString(detailsResult).asJsonObject

        // 如果沒有照片，則跳過測試
        if (!detailsJson.has("photos") || detailsJson.get("photos").isJsonNull || detailsJson.getAsJsonArray("photos").isEmpty) {
            println("Skipping test: Test location has no photos available")
            return
        }

        val photoReference = detailsJson.getAsJsonArray("photos")[0].asJsonObject.get("photoReference").asString

        val result = placesOperations.getPlacePhotoUrl(
            placeId = placeId,
            photoReference = photoReference,
            maxWidth = 800,
            maxHeight = 600
        )

        val jsonObject = JsonParser.parseString(result).asJsonObject

        // Print debug information
        println("API Response: $result")

        // 驗證
        assertTrue(jsonObject.has("success"), "Response doesn't contain 'success' field")
        assertTrue(jsonObject.get("success").asBoolean, "API call was not successful")
        assertTrue(jsonObject.has("photoUrl"), "Response doesn't contain 'photoUrl' field")
        assertNotNull(jsonObject.get("photoUrl"))
        val photoUrl = jsonObject.get("photoUrl").asString
        assertTrue(photoUrl.contains("places.googleapis.com/v1/places"))
        assertTrue(photoUrl.contains(photoReference))
    }

    @Test
    fun `getAvailablePlaceTypes should return place types list`() {
        // 執行
        val result = placesOperations.getAvailablePlaceTypes()
        val jsonObject = JsonParser.parseString(result).asJsonObject

        // 驗證
        assertTrue(jsonObject.get("success").asBoolean)
        assertNotNull(jsonObject.get("placeTypes"))
        val placeTypes = jsonObject.getAsJsonArray("placeTypes")
        assertFalse(placeTypes.isEmpty)
        assertTrue(placeTypes.size() > 50) // Google Maps有很多地點類型

        // 檢查是否包含常見的地點類型
        val typeNames = placeTypes.map { it.asJsonObject.get("name").asString }
        assertTrue(typeNames.contains("RESTAURANT"))
        assertTrue(typeNames.contains("HOSPITAL"))
    }

    @Test
    fun `calculateDistance should return distance and duration`() {
        // 台北101和台北車站的座標
        val originLat = 25.0339
        val originLng = 121.5644
        val destLat = 25.0477
        val destLng = 121.5173

        // 執行
        val result = placesOperations.calculateDistance(
            originLat = originLat,
            originLng = originLng,
            destLat = destLat,
            destLng = destLng
        )
        println("計算距離結果: $result")
        val jsonObject = JsonParser.parseString(result).asJsonObject

        // 輸出結果內容用於偵錯
        if (jsonObject.has("error")) {
            println("測試失敗，錯誤訊息: ${jsonObject.get("error").asString}")
            if (jsonObject.has("errorType")) {
                println("錯誤類型: ${jsonObject.get("errorType").asString}")
            }
        }

        // 驗證
        assertTrue(jsonObject.get("success").asBoolean, "API呼叫應該成功，但返回了失敗結果")
        assertNotNull(jsonObject.get("distance"), "返回結果中應包含distance欄位")
        assertNotNull(jsonObject.get("duration"), "返回結果中應包含duration欄位")

        val distance = jsonObject.getAsJsonObject("distance")
        val duration = jsonObject.getAsJsonObject("duration")

        assertTrue(distance.get("value").asInt > 0, "距離值應大於0")
        assertTrue(duration.get("value").asInt > 0, "時間值應大於0")
        assertNotNull(distance.get("text").asString, "距離應有文本表示")
        assertNotNull(duration.get("text").asString, "時間應有文本表示")
    }

    @Test
    fun `error handling should work when invalid parameters provided`() {
        // 執行 - 使用無效的地點類型
        val result = placesOperations.getNearbyPlaces(
            latitude = 25.033,
            longitude = 121.565,
            placeType = "INVALID_TYPE"
        )
        val jsonObject = JsonParser.parseString(result).asJsonObject

        // 驗證
        assertFalse(jsonObject.get("success").asBoolean)
        assertTrue(jsonObject.has("error"))
        assertTrue(jsonObject.get("error").asString.contains("Invalid place type"))
    }
}
