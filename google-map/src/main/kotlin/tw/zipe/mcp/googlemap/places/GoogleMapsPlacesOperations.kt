package tw.zipe.mcp.googlemap.places

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.api.gax.rpc.FixedHeaderProvider
import com.google.auth.ApiKeyCredentials
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.maps.places.v1.AutocompletePlacesRequest
import com.google.maps.places.v1.AutocompletePlacesResponse
import com.google.maps.places.v1.Circle
import com.google.maps.places.v1.GetPhotoMediaRequest
import com.google.maps.places.v1.GetPlaceRequest
import com.google.maps.places.v1.PhotoMedia
import com.google.maps.places.v1.Place
import com.google.maps.places.v1.PlacesClient
import com.google.maps.places.v1.PlacesSettings
import com.google.maps.places.v1.PriceLevel
import com.google.maps.places.v1.SearchNearbyRequest
import com.google.maps.places.v1.SearchTextRequest
import com.google.type.LatLng
import io.quarkiverse.mcp.server.Tool
import io.quarkiverse.mcp.server.ToolArg
import io.quarkus.logging.Log
import jakarta.annotation.PreDestroy
import java.io.IOException

/**
 * Google Maps Places API (New) 操作包裝類
 */
class GoogleMapsPlacesOperations {
    companion object {
        private const val API_KEY_ENV_VAR = "GOOGLE_MAPS_API_KEY"
        private const val DEFAULT_RADIUS = 500.0
        private const val DEFAULT_MAX_RESULTS = 20
        private const val DEFAULT_LANGUAGE = "zh-TW"
        private const val DEFAULT_PHOTO_MAX_WIDTH = 800
        private const val DEFAULT_PHOTO_MAX_HEIGHT = 600

        // 預設欄位掩碼路徑列表
        private val DEFAULT_PLACE_FIELD_PATHS = listOf(
            "id",
            "displayName",
            "formattedAddress",
            "location",
            "types"
        )

        // 擴展欄位掩碼路徑列表
        private val EXTENDED_PLACE_FIELD_PATHS = DEFAULT_PLACE_FIELD_PATHS + listOf(
            "rating",
            "userRatingCount",
            "websiteUri",
            "nationalPhoneNumber",
            "regularOpeningHours",
            "currentOpeningHours",
            "googleMapsUri"
        )

        private val gson: Gson =
            GsonBuilder().setFieldNamingStrategy(RemoveTrailingUnderscoreNamingStrategy()).setPrettyPrinting().create()
    }

    private val placesClient: PlacesClient

    init {
        val apiKey = System.getenv(API_KEY_ENV_VAR)
            ?: throw IllegalArgumentException("$API_KEY_ENV_VAR 環境變數未設定。")

        try {
            // 使用 API Key 創建憑證
            val credentials = ApiKeyCredentials.create(apiKey)
            // 設定 Places 客戶端設置
            val settings = PlacesSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build()

            // 創建 Places 客戶端
            placesClient = PlacesClient.create(settings)
            Log.info("Places API 客戶端初始化成功。")
        } catch (e: IOException) {
            Log.error("初始化 Places API 客戶端失敗", e)
            throw RuntimeException("初始化 Places API 客戶端失敗", e)
        }
    }

    // 當應用程式關閉時清理客戶端
    @PreDestroy
    fun cleanup() {
        try {
            placesClient.close()
            Log.info("Places API 客戶端已關閉。")
        } catch (e: Exception) {
            Log.error("關閉 Places API 客戶端時出錯", e)
        }
    }

    // ======= 回應處理工具方法 =======

    private fun Any.toSuccessResponse(): String =
        when (this) {
            is String -> this.toByteArray(Charsets.UTF_8).toString(Charsets.UTF_8)
            else -> gson.toJson(this).toByteArray(Charsets.UTF_8).toString(Charsets.UTF_8)
        }

    private fun String.toErrorResponse(data: Map<String, Any?> = emptyMap()): String {
        val errorMap = mutableMapOf<String, Any>(
            "error" to true,
            "message" to this
        )
        if (data.isNotEmpty()) {
            errorMap["data"] = data
        }
        return gson.toJson(errorMap).toByteArray(Charsets.UTF_8).toString(Charsets.UTF_8)
    }

    private inline fun <T> executeWithErrorHandling(
        errorContext: Map<String, Any?> = emptyMap(),
        operation: () -> T?
    ): String {
        return try {
            val result = operation()
            result?.toSuccessResponse() ?: "null".toSuccessResponse()
        } catch (e: Exception) {
            Log.error("操作失敗", e)
            val message = e.message ?: "未知錯誤"
            message.toErrorResponse(errorContext)
        }
    }

    /**
     * 建立指定欄位掩碼的Places客戶端
     */
    private fun createPlacesClientWithFieldMask(fields: String?, forPlaceDetails: Boolean = false): PlacesClient {
        // 處理欄位掩碼
        val fieldMaskString = formatFieldMask(fields, forPlaceDetails)

        // 建立請求頭
        val headers = mapOf("x-goog-fieldmask" to fieldMaskString)
        val headerProvider = FixedHeaderProvider.create(headers)

        // 建立新的PlacesClient
        val settings = PlacesSettings.newBuilder()
            .setCredentialsProvider(placesClient.settings.credentialsProvider)
            .setHeaderProvider(headerProvider)
            .build()

        return PlacesClient.create(settings)
    }

    /**
     * 格式化欄位掩碼為Google API需要的格式
     * @param fields 用戶提供的欄位列表，以逗號分隔
     * @param forPlaceDetails 是否用於getPlaceDetails呼叫
     * @return 格式化後的欄位掩碼字串
     */
    private fun formatFieldMask(fields: String?, forPlaceDetails: Boolean = false): String {
        // 確定要處理的欄位列表
        val fieldList = if (!fields.isNullOrEmpty()) {
            fields.split(",").map { it.trim() }
        } else {
            DEFAULT_PLACE_FIELD_PATHS
        }

        // 根據需要添加前綴並合併為字串
        return fieldList.joinToString(",") { field ->
            addPlacesPrefix(field, forPlaceDetails)
        }
    }

    /**
     * 根據情況為欄位添加"places."前綴
     * @param field 欄位名稱
     * @param forPlaceDetails 是否用於getPlaceDetails呼叫
     * @return 處理後的欄位名稱
     */
    private fun addPlacesPrefix(field: String, forPlaceDetails: Boolean): String {
        // getPlaceDetails 不需要添加前綴
        if (forPlaceDetails) {
            return field
        }

        // 其他方法需要確保有"places."前綴
        return if (field.startsWith("places.")) field else "places.$field"
    }

    /**
     * 搜尋地點
     */
    @Tool(description = "Search places using text query")
    fun searchPlaces(
        @ToolArg(description = "Text query to search places") query: String,
        @ToolArg(description = "Latitude of search center", required = false) latitude: Double? = null,
        @ToolArg(description = "Longitude of search center", required = false) longitude: Double? = null,
        @ToolArg(description = "Search radius in meters", required = false) radius: Double? = DEFAULT_RADIUS,
        @ToolArg(description = "Language code (e.g. 'zh-TW', 'en')") language: String = DEFAULT_LANGUAGE,
        @ToolArg(description = "Maximum number of results to return (max 20)") maxResults: Int = DEFAULT_MAX_RESULTS,
        @ToolArg(description = "Whether place must be currently open", required = false) openNow: Boolean? = null,
        @ToolArg(description = "Included place type (e.g. 'restaurant', 'hospital')", required = false) includedType: String? = null,
        @ToolArg(description = "Minimum rating (e.g. 4.0)", required = false) minRating: Double? = null,
        @ToolArg(description = "Price levels (comma-separated, e.g. 'MODERATE,EXPENSIVE')", required = false) priceLevels: String? = null,
        @ToolArg(description = "Return fields (comma-separated, e.g. id,displayName,formattedAddress). Call getFieldMaskDescription() to see all available fields.", required = false) fields: String? = null
    ): String {
        return executeWithErrorHandling(
            mapOf(
                "query" to query,
                "location" to if (latitude != null && longitude != null) "($latitude,$longitude)" else null,
                "radius" to radius,
                "language" to language,
                "maxResults" to maxResults,
                "openNow" to openNow,
                "includedType" to includedType,
                "minRating" to minRating,
                "priceLevels" to priceLevels,
                "fields" to fields
            )
        ) {
            try {
                // 建立帶欄位掩碼的臨時客戶端
                val clientToUse = createPlacesClientWithFieldMask(fields)

                // 構建搜尋請求
                val requestBuilder = SearchTextRequest.newBuilder()
                    .setTextQuery(query)
                    .setLanguageCode(language)
                    .setMaxResultCount(maxResults)

                // 如果提供了位置，設定坐標和半徑
                if (latitude != null && longitude != null) {
                    val locationBias = SearchTextRequest.LocationBias.newBuilder()
                        .setCircle(
                            Circle.newBuilder()
                                .setCenter(
                                    LatLng.newBuilder()
                                        .setLatitude(latitude)
                                        .setLongitude(longitude)
                                        .build()
                                )
                                .setRadius(radius ?: DEFAULT_RADIUS)
                                .build()
                        )
                        .build()
                    requestBuilder.setLocationBias(locationBias)
                }

                // 如果指定了開放狀態
                openNow?.let { requestBuilder.setOpenNow(it) }

                // 如果指定了場所類型
                if (!includedType.isNullOrEmpty()) {
                    requestBuilder.setIncludedType(includedType)
                }

                // 如果指定了最低評分
                minRating?.let { requestBuilder.setMinRating(it) }

                // 如果指定了價格等級
                if (!priceLevels.isNullOrEmpty()) {
                    priceLevels.split(",").forEach { level ->
                        when (level.trim().uppercase()) {
                            "FREE" -> requestBuilder.addPriceLevels(PriceLevel.PRICE_LEVEL_FREE)
                            "INEXPENSIVE" -> requestBuilder.addPriceLevels(PriceLevel.PRICE_LEVEL_INEXPENSIVE)
                            "MODERATE" -> requestBuilder.addPriceLevels(PriceLevel.PRICE_LEVEL_MODERATE)
                            "EXPENSIVE" -> requestBuilder.addPriceLevels(PriceLevel.PRICE_LEVEL_EXPENSIVE)
                            "VERY_EXPENSIVE" -> requestBuilder.addPriceLevels(PriceLevel.PRICE_LEVEL_VERY_EXPENSIVE)
                        }
                    }
                }

                // 執行搜尋
                val response = clientToUse.searchText(requestBuilder.build())

                // 關閉臨時客戶端
                clientToUse.close()

                // 處理結果
                val results = response.placesList.map { place ->
                    mapOf(
                        "id" to place.id,
                        "name" to place.displayName?.text,
                        "formattedAddress" to place.formattedAddress,
                        "location" to place.location?.let {
                            mapOf(
                                "latitude" to it.latitude,
                                "longitude" to it.longitude
                            )
                        },
                        "types" to place.typesList,
                        "rating" to if (place.rating != 0.0) place.rating else null,
                        "priceLevel" to if (place.priceLevel != PriceLevel.PRICE_LEVEL_UNSPECIFIED) place.priceLevel.name else null
                    ).filterValues { it != null }
                }

                mapOf(
                    "results" to results,
                    "totalResults" to response.placesList.size
                )
            } catch (e: Exception) {
                Log.error("以文字搜尋地點時發生錯誤", e)
                throw e
            }
        }
    }

    @Tool(description = "Get nearby places based on location")
    fun getNearbyPlaces(
        @ToolArg(description = "Latitude of search center") latitude: Double,
        @ToolArg(description = "Longitude of search center") longitude: Double,
        @ToolArg(description = "Search radius in meters") radius: Double = DEFAULT_RADIUS,
        @ToolArg(description = "Included primary place type (e.g. 'restaurant', 'hospital')", required = false) includedPrimaryType: String? = null,
        @ToolArg(description = "Language code (e.g. 'zh-TW', 'en')") language: String = DEFAULT_LANGUAGE,
        @ToolArg(description = "Maximum number of results to return (max 20)") maxResults: Int = DEFAULT_MAX_RESULTS,
        @ToolArg(description = "Ranking preference (DISTANCE or POPULARITY, default is not set)", required = false) rankPreference: String? = null,
        @ToolArg(description = "Return fields (comma-separated, e.g. id,displayName,formattedAddress). Call getFieldMaskDescription() to see all available fields.", required = false) fields: String? = null
    ): String {
        return executeWithErrorHandling(
            mapOf(
                "location" to "($latitude,$longitude)",
                "radius" to radius,
                "includedPrimaryType" to includedPrimaryType,
                "language" to language,
                "maxResults" to maxResults,
                "rankPreference" to rankPreference,
                "fields" to fields
            )
        ) {
            try {
                // 建立帶欄位掩碼的臨時客戶端
                val clientToUse = createPlacesClientWithFieldMask(fields)

                // 構建搜尋請求
                val requestBuilder = SearchNearbyRequest.newBuilder()
                    .setLanguageCode(language)
                    .setMaxResultCount(maxResults)
                    .setLocationRestriction(
                        SearchNearbyRequest.LocationRestriction.newBuilder()
                            .setCircle(
                                Circle.newBuilder()
                                    .setCenter(
                                        LatLng.newBuilder()
                                            .setLatitude(latitude)
                                            .setLongitude(longitude)
                                            .build()
                                    )
                                    .setRadius(radius)
                                    .build()
                            )
                            .build()
                    )

                // 如果指定了主要類型
                if (!includedPrimaryType.isNullOrEmpty()) {
                    requestBuilder.addIncludedPrimaryTypes(includedPrimaryType)
                }

                // 設定排序偏好
                if (!rankPreference.isNullOrEmpty()) {
                    when (rankPreference.uppercase()) {
                        "DISTANCE" -> requestBuilder.setRankPreference(SearchNearbyRequest.RankPreference.DISTANCE)
                        "POPULARITY" -> requestBuilder.setRankPreference(SearchNearbyRequest.RankPreference.POPULARITY)
                    }
                }

                // 執行搜尋
                val response = clientToUse.searchNearby(requestBuilder.build())

                // 關閉臨時客戶端
                clientToUse.close()

                // 處理結果
                val results = response.placesList.map { place ->
                    mapOf(
                        "id" to place.id,
                        "name" to place.displayName?.text,
                        "formattedAddress" to place.formattedAddress,
                        "location" to place.location?.let {
                            mapOf(
                                "latitude" to it.latitude,
                                "longitude" to it.longitude
                            )
                        },
                        "types" to place.typesList
                    ).filterValues { it != null }
                }

                mapOf(
                    "results" to results,
                    "totalResults" to response.placesList.size
                )
            } catch (e: Exception) {
                Log.error("搜尋附近地點時發生錯誤", e)
                throw e
            }
        }
    }

    @Tool(description = "Get place autocomplete suggestions")
    fun getPlaceAutocomplete(
        @ToolArg(description = "Text input for autocomplete") input: String,
        @ToolArg(description = "Latitude of preferred location center", required = false) latitude: Double? = null,
        @ToolArg(description = "Longitude of preferred location center", required = false) longitude: Double? = null,
        @ToolArg(description = "Location preference radius in meters", required = false) radius: Double? = null,
        @ToolArg(description = "Included primary type (e.g. 'establishment', 'geocode')", required = false) includedPrimaryType: String? = null,
        @ToolArg(description = "Language code (e.g. 'zh-TW', 'en')") language: String = DEFAULT_LANGUAGE
    ): String {
        return executeWithErrorHandling(
            mapOf(
                "input" to input,
                "location" to if (latitude != null && longitude != null) "($latitude,$longitude)" else null,
                "radius" to radius,
                "includedPrimaryType" to includedPrimaryType,
                "language" to language
            )
        ) {
            // 構建自動完成請求
            val requestBuilder = AutocompletePlacesRequest.newBuilder()
                .setInput(input)
                .setLanguageCode(language)

            // 如果提供了位置，設定位置偏好
            if (latitude != null && longitude != null) {
                val locationBias = AutocompletePlacesRequest.LocationBias.newBuilder()
                    .setCircle(
                        Circle.newBuilder()
                            .setCenter(
                                LatLng.newBuilder()
                                    .setLatitude(latitude)
                                    .setLongitude(longitude)
                                    .build()
                            )
                            .setRadius(radius ?: DEFAULT_RADIUS)
                            .build()
                    )
                    .build()
                requestBuilder.setLocationBias(locationBias)
            }

            // 如果指定了主要類型
            if (!includedPrimaryType.isNullOrEmpty()) {
                requestBuilder.addIncludedPrimaryTypes(includedPrimaryType)
            }

            // 執行自動完成
            val response = placesClient.autocompletePlaces(requestBuilder.build())

            val suggestions = response.suggestionsList.map { suggestion: AutocompletePlacesResponse.Suggestion ->
                val placePrediction = suggestion.placePrediction
                mapOf(
                    "placeId" to placePrediction.placeId,
                    "text" to placePrediction.text.text,
                    "matchedSubstrings" to placePrediction.text.matchesList.map { match ->
                        mapOf(
                            "start" to match.startOffset,
                            "end" to match.endOffset
                        )
                    },
                    "types" to placePrediction.typesList
                )
            }

            mapOf(
                "suggestions" to suggestions,
                "totalSuggestions" to response.suggestionsList.size
            )
        }
    }

    @Tool(description = "Get place details by place ID")
    fun getPlaceDetails(
        @ToolArg(description = "Google Place ID") placeId: String,
        @ToolArg(description = "Language code (e.g. 'zh-TW', 'en')") language: String = DEFAULT_LANGUAGE,
        @ToolArg(description = "Region code (e.g. 'TW', 'US')", required = false) regionCode: String? = null
    ): String {
        return executeWithErrorHandling(
            mapOf(
                "placeId" to placeId,
                "language" to language,
                "regionCode" to regionCode
            )
        ) {
            // 構建地點名稱字串，格式為 "places/YOUR_PLACE_ID"
            val placeName = "places/$placeId"

            val fields =
                "id,nationalPhoneNumber,internationalPhoneNumber,formattedAddress,location,googleMapsUri,regularOpeningHours,regularOpeningHours,userRatingCount,displayName,reviews,photos,googleMapsLinks"
            try {
                // 建立帶欄位掩碼的臨時客戶端
                val tempClient = createPlacesClientWithFieldMask(fields, true)

                // 構建請求
                val requestBuilder = GetPlaceRequest.newBuilder()
                    .setName(placeName)
                    .setLanguageCode(language)

                // 如果提供了地區代碼，則設定
                if (!regionCode.isNullOrEmpty()) {
                    requestBuilder.setRegionCode(regionCode)
                }

                val request = requestBuilder.build()

                // 調用 Places API 客戶端的 getPlace 方法
                val response: Place = tempClient.getPlace(request)

                // 關閉臨時客戶端
                tempClient.close()
                val json = gson.toJson(response)
                val resultMap = gson.fromJson<Map<String, Any?>>(
                    json,
                    object : com.google.gson.reflect.TypeToken<Map<String, Any?>>() {}.type
                )
                val filteredMap = resultMap.filterValues { value ->
                    when (value) {
                        null -> false
                        is String -> value.isNotEmpty()
                        is Map<*, *> -> value.isNotEmpty()
                        is Collection<*> -> value.isNotEmpty()
                        is Number -> when (value) {
                            is Double -> value != 0.0
                            is Int, is Long -> value != 0
                            else -> true
                        }
                        else -> true
                    }
                }

                filteredMap
            } catch (e: Exception) {
                Log.error("獲取地點詳情失敗", e)
                throw e
            }
        }
    }

    @Tool(description = "Get Google Places photo")
    fun getPlacePhoto(
        @ToolArg(description = "Photo resource name (e.g. places/PLACE_ID/photos/PHOTO_REFERENCE)") photoName: String,
        @ToolArg(description = "Maximum width of the image") maxWidth: Int = DEFAULT_PHOTO_MAX_WIDTH,
        @ToolArg(description = "Maximum height of the image") maxHeight: Int = DEFAULT_PHOTO_MAX_HEIGHT
    ): String {
        return executeWithErrorHandling(
            mapOf(
                "photoName" to photoName,
                "maxWidth" to maxWidth,
                "maxHeight" to maxHeight
            )
        ) {
            // 構建照片請求
            val request = GetPhotoMediaRequest.newBuilder()
                .setName("$photoName/media")
                .setMaxWidthPx(maxWidth)
                .setMaxHeightPx(maxHeight)
                .build()

            val response: PhotoMedia = placesClient.getPhotoMedia(request)

            // 返回包含照片URI的Map
            mapOf(
                "name" to response.name,
                "photoUri" to if (response.photoUri.isNotEmpty()) response.photoUri else null
            ).filterValues { it != null }
        }
    }

    /**
     * 獲取 Google Places API 欄位掩碼的詳細描述
     * @return 包含所有欄位名稱及其描述的映射
     */
    @Tool(description = "Get detailed description of Google Places API field masks, returning a JSON object with 'fields' containing a map of field names and their descriptions")
    fun getFieldMaskDescription(): String {
        // 欄位名及其描述的映射
        val fieldsMap = mapOf(
            // Place Details Essentials IDs Only SKU 欄位
            // 歸屬資訊
            "attributions" to "Attribution Information",
            // 地點的唯一識別符
            "id" to "Place's Unique Identifier",
            // 地點資源名稱
            "name" to "Place Resource Name, format: places/PLACE_ID",
            // 與地點相關的照片集
            "photos" to "Collection of Place Photos",

            // Place Details Essentials SKU Fields
            // 地址的結構化組件
            "addressComponents" to "Structured Address Components",
            // 格式化地址
            "adrFormatAddress" to "Formatted Address",
            // 顯示為單行文字的完整地址
            "formattedAddress" to "Complete Address as Single Line",
            // 地理坐標
            "location" to "Geographic Coordinates (Lat/Lng)",
            // 短格式地址
            "shortFormattedAddress" to "Short Format Address",
            // 建議的查看區域
            "viewport" to "Suggested Viewing Area",

            // Place Details Pro SKU Fields
            // 無障礙設施選項
            "accessibilityOptions" to "Accessibility Facility Options",
            // 商業運營狀態
            "businessStatus" to "Business Operation Status",
            // 地點的顯示名稱
            "displayName" to "Place Display Name",
            // Google Maps 連結
            "googleMapsLinks" to "Google Maps Links (Pre-release)",
            // Google Maps URI
            "googleMapsUri" to "Google Maps URI",

            // Place Details Enterprise SKU Fields
            // 國家電話號碼
            "nationalPhoneNumber" to "National Phone Number",
            // 價格等級
            "priceLevel" to "Price Level",
            // 價格範圍
            "priceRange" to "Price Range",
            // 評分
            "rating" to "Rating Score",
            // 常規營業時間
            "regularOpeningHours" to "Regular Business Hours",
            // 常規次要營業時間
            "regularSecondaryOpeningHours" to "Secondary Regular Business Hours",
            // 用戶評分數量
            "userRatingCount" to "Number of User Ratings",
            // 網站 URI
            "websiteUri" to "Website URI",

            // Place Details Enterprise + Atmosphere SKU Fields
            // 是否允許攜帶狗
            "allowsDogs" to "Dogs Allowed",
            // 是否提供堂食服務
            "dineIn" to "Dine-in Service Available",
            // 編輯摘要
            "editorialSummary" to "Editorial Summary",
            // 是否適合兒童
            "goodForChildren" to "Child-friendly",
            // 是否適合團體
            "goodForGroups" to "Group-friendly",
            // 是否有兒童菜單
            "menuForChildren" to "Children's Menu Available",
            // 停車選項
            "parkingOptions" to "Parking Options",
            // 是否提供戶外座位
            "outdoorSeating" to "Outdoor Seating Available",
            // 是否可預訂
            "reservable" to "Accepts Reservations",
            // 是否有洗手間
            "restroom" to "Restroom Available",
            // 評論
            "reviews" to "Reviews",
            // 路線摘要
            "routingSummaries" to "Route Summaries (Text/Nearby Search Only)",
            // 是否提供早餐
            "servesBreakfast" to "Serves Breakfast",
            // 是否提供早午餐
            "servesBrunch" to "Serves Brunch",
            // 是否提供咖啡
            "servesCoffee" to "Serves Coffee",
            // 是否提供甜點
            "servesDessert" to "Serves Dessert",
            // 是否提供晚餐
            "servesDinner" to "Serves Dinner",
            // 是否提供午餐
            "servesLunch" to "Serves Lunch",
            // 是否提供素食
            "servesVegetarianFood" to "Serves Vegetarian Food",
            // 是否提供外帶服務
            "takeout" to "Takeout Available"
        )

        return gson.toJson(mapOf("fields" to fieldsMap))
    }
}
