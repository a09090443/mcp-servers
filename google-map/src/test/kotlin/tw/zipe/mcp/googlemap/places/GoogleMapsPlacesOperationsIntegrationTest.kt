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

//    @Test
    fun `searchPlaces should return results for valid query`() {
        // 執行
        val result = placesOperations.searchPlaces("台北101")
        val jsonObject = JsonParser.parseString(result).asJsonObject

        // 驗證 - 根據實際返回格式調整
        assertFalse(jsonObject.has("error"))
        assertTrue(jsonObject.has("results"))
        val places = jsonObject.getAsJsonArray("results")
        assertFalse(places.isEmpty)

        // 檢查第一個結果
        val firstPlace = places[0].asJsonObject
        assertNotNull(firstPlace.get("name"))
        assertNotNull(firstPlace.get("id"))
        assertNotNull(firstPlace.get("formattedAddress"))
    }

//    @Test
    fun `searchPlaces should return results with location parameter`() {
        // 三芝市區座標
        val latitude = 25.2576
        val longitude = 121.5009

        // 執行
        val result = placesOperations.searchPlaces(
            query = "咖啡廳",
            latitude = latitude,
            longitude = longitude,
            radius = 5000.0,
            language = "zh-TW",
            maxResults = 10,
            openNow = false,
            includedType = "cafe",
            minRating = 0.0,
            fields = "id,displayName,formattedAddress,rating,userRatingCount,regularOpeningHours"
        )
        val jsonObject = JsonParser.parseString(result).asJsonObject

        // 驗證
        assertFalse(jsonObject.has("error"))
        assertTrue(jsonObject.has("results"))
        val places = jsonObject.getAsJsonArray("results")
        assertFalse(places.isEmpty)
    }

//    @Test
    fun `searchPlaces should handle includedType parameter`() {
        // 執行
        val result = placesOperations.searchPlaces(
            query = "台北",
            includedType = "restaurant",
            maxResults = 10
        )
        val jsonObject = JsonParser.parseString(result).asJsonObject

        // 驗證
        assertFalse(jsonObject.has("error"))
        assertTrue(jsonObject.has("results"))
        val places = jsonObject.getAsJsonArray("results")
        assertFalse(places.isEmpty)
    }

//    @Test
    fun `searchPlaces should handle priceLevel parameter`() {
        // 執行
        val result = placesOperations.searchPlaces(
            query = "台北 餐廳",
            priceLevels = "MODERATE,EXPENSIVE"
        )
        val jsonObject = JsonParser.parseString(result).asJsonObject

        // 驗證
        assertFalse(jsonObject.has("error"))
        assertTrue(jsonObject.has("results"))
    }

//    @Test
    fun `getNearbyPlaces should return places near given location`() {
        // 台北市座標
        val latitude = 25.033
        val longitude = 121.565

        // 執行
        val result = placesOperations.getNearbyPlaces(
            latitude = latitude,
            longitude = longitude,
            radius = 5000.0,
            includedPrimaryType = "restaurant",
            language = "zh-TW",
            maxResults = 10
        )
        val jsonObject = JsonParser.parseString(result).asJsonObject

        // 驗證
        assertFalse(jsonObject.has("error"))
        assertTrue(jsonObject.has("results"))
        val places = jsonObject.getAsJsonArray("results")
        assertFalse(places.isEmpty)
    }

//    @Test
    fun `getNearbyPlaces should handle rankPreference parameter`() {
        // 台北市座標
        val latitude = 25.033
        val longitude = 121.565

        // 執行
        val result = placesOperations.getNearbyPlaces(
            latitude = latitude,
            longitude = longitude,
            radius = 3000.0,
            rankPreference = "DISTANCE"
        )
        val jsonObject = JsonParser.parseString(result).asJsonObject

        // 驗證
        assertFalse(jsonObject.has("error"))
        assertTrue(jsonObject.has("results"))
    }

//    @Test
    fun `getPlaceAutocomplete should return suggestions for input`() {
        // 執行
        val result = placesOperations.getPlaceAutocomplete("台北1")
        val jsonObject = JsonParser.parseString(result).asJsonObject

        // 驗證
        assertFalse(jsonObject.has("error"))
        assertTrue(jsonObject.has("suggestions"))
        val suggestions = jsonObject.getAsJsonArray("suggestions")
        assertFalse(suggestions.isEmpty)
    }

//    @Test
    fun `getPlaceAutocomplete should handle location bias`() {
        // 台北市座標
        val latitude = 25.033
        val longitude = 121.565

        // 執行
        val result = placesOperations.getPlaceAutocomplete(
            input = "餐廳",
            latitude = latitude,
            longitude = longitude,
            radius = 5000.0
        )
        val jsonObject = JsonParser.parseString(result).asJsonObject

        // 驗證
        assertFalse(jsonObject.has("error"))
        assertTrue(jsonObject.has("suggestions"))
    }

//    @Test
    fun `getPlaceAutocomplete should handle includedPrimaryType parameter`() {
        // 執行
        val result = placesOperations.getPlaceAutocomplete(
            input = "台北",
            includedPrimaryType = "establishment"
        )
        val jsonObject = JsonParser.parseString(result).asJsonObject

        // 驗證
        assertFalse(jsonObject.has("error"))
        assertTrue(jsonObject.has("suggestions"))
    }

//    @Test
    fun `getPlaceDetails should return details for valid placeId`() {
        // 先通過搜索獲取有效的placeId
        val searchResult = placesOperations.searchPlaces("台北101")
        val searchJson = JsonParser.parseString(searchResult).asJsonObject

        // 檢查搜索結果是否有錯誤
        if (searchJson.has("error") || searchJson.getAsJsonArray("results").isEmpty) {
            println("搜索台北101失敗或無結果，跳過地點詳情測試")
            return
        }

        // 獲取第一個結果的placeId
        val placeId = searchJson.getAsJsonArray("results")[0].asJsonObject.get("id").asString
        assertNotNull(placeId, "無法獲取有效的placeId")

        // 執行
        val result = placesOperations.getPlaceDetails(placeId)
        val jsonObject = JsonParser.parseString(result).asJsonObject

        // 驗證
        assertFalse(
            jsonObject.has("error"),
            "錯誤信息: ${if (jsonObject.has("message")) jsonObject.get("message").asString else "無"}"
        )
        assertEquals(placeId, jsonObject.get("id").asString)
        assertTrue(jsonObject.has("displayName"))
        assertTrue(jsonObject.has("formattedAddress"))
    }

//    @Test
    fun `getPlaceDetails should handle extended fields`() {
        // 先通過搜索獲取有效的placeId
        val searchResult = placesOperations.searchPlaces("台北101")
        val searchJson = JsonParser.parseString(searchResult).asJsonObject

        // 檢查搜索結果
        if (searchJson.has("error") || searchJson.getAsJsonArray("results").isEmpty) {
            println("搜索台北101失敗或無結果，跳過地點詳情測試")
            return
        }

        val placeId = searchJson.getAsJsonArray("results")[0].asJsonObject.get("id").asString

        // 執行，請求擴展欄位
        val result = placesOperations.getPlaceDetails(
            placeId
        )
        val jsonObject = JsonParser.parseString(result).asJsonObject

        // 驗證
        assertFalse(jsonObject.has("error"))
        assertEquals(placeId, jsonObject.get("id").asString)

        // 檢查至少一個擴展欄位是否存在
        val hasExtendedField = jsonObject.has("rating") ||
                jsonObject.has("websiteUri") ||
                jsonObject.has("nationalPhoneNumber")
        assertTrue(hasExtendedField)
    }

//    @Test
    fun `getPlacePhoto should return photo URL`() {
        // 先通過搜索獲取一個有效的placeId
        val searchResult = placesOperations.searchPlaces("台北101")
        val searchJson = JsonParser.parseString(searchResult).asJsonObject

        // 檢查搜索結果是否有錯誤
        if (searchJson.has("error") || searchJson.getAsJsonArray("results").isEmpty) {
            println("搜索台北101失敗或無結果，跳過照片測試")
            return
        }

        // 獲取第一個結果的placeId
        val placeId = searchJson.getAsJsonArray("results")[0].asJsonObject.get("id").asString

        // 獲取地點詳情，包含照片
        val detailsResult = placesOperations.getPlaceDetails(placeId)
        val detailsJson = JsonParser.parseString(detailsResult).asJsonObject

        // 檢查是否有照片
        if (!detailsJson.has("photos") || detailsJson.getAsJsonArray("photos").isEmpty) {
            println("此地點沒有照片，跳過照片測試")
            return
        }

        val photos = detailsJson.getAsJsonArray("photos")
        // 獲取第一張照片的名稱
        val photoName = photos[0].asJsonObject.get("name").asString

        // 執行
        val photoResult = placesOperations.getPlacePhoto(photoName, 600, 400)
        val photoJson = JsonParser.parseString(photoResult).asJsonObject

        // 驗證
        assertFalse(
            photoJson.has("error"),
            "錯誤信息: ${if (photoJson.has("message")) photoJson.get("message").asString else "無"}"
        )
        assertTrue(photoJson.has("photoUri"))
        assertTrue(photoJson.get("photoUri").asString.startsWith("http"), "照片URL格式不正確")
    }

//    @Test
    fun `getFieldMaskDescription should return field descriptions`() {
        val result = placesOperations.getFieldMaskDescription()
        val jsonObject = JsonParser.parseString(result).asJsonObject

        assertTrue(jsonObject.has("fields"))
        val fields = jsonObject.getAsJsonObject("fields")
        assertFalse(fields.entrySet().isEmpty())

        // 檢查一些關鍵欄位是否存在
        assertTrue(fields.has("id"))
        assertTrue(fields.has("formattedAddress"))
        assertTrue(fields.has("rating"))
    }

//    @Test
    fun `error handling should work when invalid parameters provided`() {
        // 執行 - 使用無效的地點ID
        val result = placesOperations.getPlaceDetails("invalid_place_id_12345")
        val jsonObject = JsonParser.parseString(result).asJsonObject

        // 驗證
        assertTrue(jsonObject.has("error"))
        assertTrue(jsonObject.get("error").asBoolean)
        assertTrue(jsonObject.has("message"))
    }
}
