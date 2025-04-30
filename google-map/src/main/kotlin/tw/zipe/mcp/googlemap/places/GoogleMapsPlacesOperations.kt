package tw.zipe.mcp.googlemap.places

import com.google.gson.Gson
import com.google.maps.FindPlaceFromTextRequest
import com.google.maps.GeoApiContext
import com.google.maps.PlacesApi
import com.google.maps.model.LatLng
import com.google.maps.model.OpeningHours
import com.google.maps.model.PlaceDetails
import com.google.maps.model.PlaceType
import com.google.maps.model.TravelMode
import io.quarkiverse.mcp.server.Tool
import io.quarkiverse.mcp.server.ToolArg
import jakarta.enterprise.context.ApplicationScoped
import java.util.concurrent.TimeUnit

/**
 * Google Maps Places API 操作封裝類
 */
@ApplicationScoped
class GoogleMapsPlacesOperations {
    companion object {
        private const val API_KEY_ENV_VAR = "GOOGLE_MAPS_API_KEY"
        private const val DEFAULT_RADIUS = 5000
        private const val DEFAULT_MAX_RESULTS = 20
        private const val DEFAULT_LANGUAGE = "zh-TW"
        private const val DEFAULT_PHOTO_MAX_WIDTH = 800
        private const val DEFAULT_PHOTO_MAX_HEIGHT = 600
        private const val MAPS_PHOTO_URL_BASE = "https://maps.googleapis.com/maps/api/place/photo"

        private const val CONNECT_TIMEOUT_SECONDS = 10L
        private const val READ_TIMEOUT_SECONDS = 10L

        private val gson = Gson()
        private val PLACE_TYPES = PlaceType.entries.associateBy { it.name }
    }

    private val geoApiContext: GeoApiContext

    init {
        val apiKey = System.getenv(API_KEY_ENV_VAR)
            ?: throw IllegalArgumentException("$API_KEY_ENV_VAR environment variable is not set.")

        geoApiContext = GeoApiContext.Builder()
            .apiKey(apiKey)
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    // ======= 響應處理工具方法 =======

    private fun Map<String, Any?>.toSuccessResponse(): String {
        val responseMap = mutableMapOf<String, Any?>("success" to true)
        responseMap.putAll(this)
        return gson.toJson(responseMap)
    }

    private fun String.toErrorResponse(data: Map<String, Any?> = emptyMap()): String {
        val responseMap = mutableMapOf<String, Any?>("success" to false, "error" to this)
        responseMap.putAll(data)
        return gson.toJson(responseMap)
    }

    private inline fun <T> executeWithErrorHandling(
        errorContext: Map<String, Any?> = emptyMap(),
        operation: () -> T
    ): String {
        return try {
            val result = operation()
            when (result) {
                is Map<*, *> -> (result as Map<String, Any?>).toSuccessResponse()
                is String -> mapOf("result" to result).toSuccessResponse()
                else -> mapOf("result" to result).toSuccessResponse()
            }
        } catch (e: Exception) {
            (e.message ?: "操作失敗").toErrorResponse(errorContext)
        }
    }

    // ======= 地點搜索相關工具 =======

    @Tool(description = "Search for places by text query")
    fun searchPlaces(
        @ToolArg(description = "Text query to search for places") query: String,
        @ToolArg(description = "Latitude of the search center (optional)") latitude: Double? = null,
        @ToolArg(description = "Longitude of the search center (optional)") longitude: Double? = null,
        @ToolArg(description = "Radius in meters to search within (max 50000)") radius: Int = DEFAULT_RADIUS,
        @ToolArg(description = "Language code (e.g., 'zh-TW', 'en')") language: String = DEFAULT_LANGUAGE,
        @ToolArg(description = "Maximum number of results to return") maxResults: Int = DEFAULT_MAX_RESULTS
    ): String = executeWithErrorHandling(mapOf("query" to query)) {
        val request = PlacesApi.textSearchQuery(geoApiContext, query).language(language)

        if (latitude != null && longitude != null) {
            request.location(LatLng(latitude, longitude)).radius(radius)
        }

        val response = request.await()

        val places = response.results.take(maxResults).map { place ->
            mapOf(
                "placeId" to place.placeId,
                "name" to place.name,
                "address" to place.formattedAddress,
                "location" to mapOf(
                    "lat" to place.geometry.location.lat,
                    "lng" to place.geometry.location.lng
                ),
                "rating" to place.rating,
                "types" to place.types
            )
        }

        mapOf(
            "places" to places,
            "totalCount" to places.size,
            "query" to query,
            "hasMoreResults" to (response.nextPageToken != null)
        )
    }

    @Tool(description = "Get nearby places based on location and place type")
    fun getNearbyPlaces(
        @ToolArg(description = "Latitude of the search center") latitude: Double,
        @ToolArg(description = "Longitude of the search center") longitude: Double,
        @ToolArg(description = "Radius in meters to search within (max 50000)") radius: Int = DEFAULT_RADIUS,
        @ToolArg(description = "Place type (e.g., RESTAURANT, HOSPITAL, BANK)") placeType: String? = null,
        @ToolArg(description = "Whether the place should be open now") openNow: Boolean = false,
        @ToolArg(description = "Keyword to filter results") keyword: String? = null,
        @ToolArg(description = "Language code (e.g., 'zh-TW', 'en')") language: String = DEFAULT_LANGUAGE,
        @ToolArg(description = "Maximum number of results to return") maxResults: Int = DEFAULT_MAX_RESULTS
    ): String = executeWithErrorHandling(
        mapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "radius" to radius,
            "placeType" to placeType
        )
    ) {
        val location = LatLng(latitude, longitude)
        val request = PlacesApi.nearbySearchQuery(geoApiContext, location)
            .radius(radius)
            .language(language)

        if (!placeType.isNullOrBlank()) {
            val type = PLACE_TYPES[placeType.uppercase()]
                ?: throw IllegalArgumentException("Invalid place type: $placeType")
            request.type(type)
        }

        if (openNow) {
            request.openNow(true)
        }

        if (!keyword.isNullOrBlank()) {
            request.keyword(keyword)
        }

        val response = request.await()

        val places = response.results.take(maxResults).map { place ->
            mapOf(
                "placeId" to place.placeId,
                "name" to place.name,
                "address" to place.vicinity,
                "location" to mapOf(
                    "lat" to place.geometry.location.lat,
                    "lng" to place.geometry.location.lng
                ),
                "rating" to place.rating,
                "types" to place.types,
                "openNow" to place.openingHours?.openNow
            )
        }

        mapOf(
            "places" to places,
            "totalCount" to places.size,
            "location" to mapOf("lat" to latitude, "lng" to longitude),
            "radius" to radius,
            "placeType" to placeType,
            "hasMoreResults" to (response.nextPageToken != null)
        )
    }

    @Tool(description = "Find place from text query with place type restriction")
    fun findPlaceFromText(
        @ToolArg(description = "Text input to search for place") input: String,
        @ToolArg(description = "Input type: textQuery or phoneNumber") inputType: String = "textQuery",
        @ToolArg(description = "Fields to include in the response (comma-separated)") fields: String = "place_id,name,formatted_address,geometry,type",
        @ToolArg(description = "Language code (e.g., 'zh-TW', 'en')") language: String = DEFAULT_LANGUAGE
    ): String = executeWithErrorHandling(mapOf("input" to input, "inputType" to inputType)) {
        val inputTypeEnum = when (inputType.lowercase()) {
            "phonenumber" -> FindPlaceFromTextRequest.InputType.PHONE_NUMBER
            else -> FindPlaceFromTextRequest.InputType.TEXT_QUERY
        }

        val findPlaceRequest = PlacesApi.findPlaceFromText(
            geoApiContext,
            input,
            inputTypeEnum
        )

        // 轉換欄位為需要的格式
        val fieldsList = fields.split(",").map { field ->
            try {
                when (field.trim().lowercase()) {
                    "place_id" -> FindPlaceFromTextRequest.FieldMask.PLACE_ID
                    "name" -> FindPlaceFromTextRequest.FieldMask.NAME
                    "formatted_address" -> FindPlaceFromTextRequest.FieldMask.FORMATTED_ADDRESS
                    "geometry" -> FindPlaceFromTextRequest.FieldMask.GEOMETRY
                    "type" -> FindPlaceFromTextRequest.FieldMask.TYPES
                    else -> FindPlaceFromTextRequest.FieldMask.valueOf(field.trim().uppercase())
                }
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid field: $field")
            }
        }.toTypedArray()

        val response = findPlaceRequest
            .fields(*fieldsList)
            .language(language)
            .await()

        val places = response.candidates.map { place ->
            mapOf(
                "placeId" to place.placeId,
                "name" to place.name,
                "formattedAddress" to place.formattedAddress,
                "location" to place.geometry?.location?.let { location ->
                    mapOf("lat" to location.lat, "lng" to location.lng)
                },
                "types" to place.types
            )
        }

        mapOf(
            "places" to places,
            "totalCount" to places.size,
            "input" to input,
            "inputType" to inputType
        )
    }

    // ======= 地點詳情相關工具 =======

    @Tool(description = "Get detailed information about a place by its Place ID")
    fun getPlaceDetails(
        @ToolArg(description = "Google Place ID") placeId: String,
        @ToolArg(description = "Language code (e.g., 'zh-TW', 'en')") language: String = DEFAULT_LANGUAGE
    ): String = executeWithErrorHandling<Map<String, Any?>>(mapOf("placeId" to placeId)) {
        val place: PlaceDetails = PlacesApi.placeDetails(geoApiContext, placeId)
            .language(language)
            .await()

        // 創建營業時間的可讀表示
        val openingHours = place.openingHours?.periods?.mapNotNull { period: OpeningHours.Period ->
            val openTime = period.open?.time
            val closeTime = period.close?.time

            if (openTime != null && period.open != null) {
                val day = period.open.day.name
                "$day ${openTime}-${closeTime ?: "24:00"}"
            } else {
                null
            }
        }

        mapOf(
            "placeId" to place.placeId,
            "name" to place.name,
            "formattedAddress" to place.formattedAddress,
            "internationalPhoneNumber" to place.internationalPhoneNumber,
            "rating" to place.rating,
            "userRatingsTotal" to place.userRatingsTotal,
            "website" to place.website?.toString(),
            "location" to mapOf<String, Double?>(
                "lat" to place.geometry?.location?.lat,
                "lng" to place.geometry?.location?.lng
            ),
            "types" to place.types?.map { it.toString() },
            "formattedPhoneNumber" to place.formattedPhoneNumber,
            "openingHours" to openingHours,
            "weekdayText" to place.openingHours?.weekdayText?.toList(),
            "photos" to place.photos?.map { photo ->
                mapOf(
                    "photoReference" to photo.photoReference,
                    "height" to photo.height,
                    "width" to photo.width,
                    "attributions" to photo.htmlAttributions?.toList()
                )
            },
            "priceLevel" to place.priceLevel?.toString(),
            "reviews" to place.reviews?.map { review ->
                mapOf<String, Any?>(
                    "authorName" to review.authorName,
                    "rating" to review.rating,
                    "text" to review.text,
                    "time" to review.time?.toEpochMilli(),
                    "relativeTimeDescription" to review.relativeTimeDescription
                )
            }
        )
    }

    @Tool(description = "Get photo URL for a Google Places photo reference")
    fun getPlacePhotoUrl(
        @ToolArg(description = "Place ID that the photo belongs to") placeId: String,
        @ToolArg(description = "Photo reference from a place details result") photoReference: String,
        @ToolArg(description = "Maximum width of the image") maxWidth: Int = DEFAULT_PHOTO_MAX_WIDTH,
        @ToolArg(description = "Maximum height of the image") maxHeight: Int = DEFAULT_PHOTO_MAX_HEIGHT
    ): String = executeWithErrorHandling(mapOf("placeId" to placeId, "photoReference" to photoReference)) {
        val apiKey = System.getenv(API_KEY_ENV_VAR)
        val photoUrl = "https://places.googleapis.com/v1/places/$placeId/photos/$photoReference/media" +
                "?maxHeightPx=$maxHeight" +
                "&maxWidthPx=$maxWidth" +
                "&key=$apiKey"

        mapOf(
            "photoUrl" to photoUrl,
            "photoReference" to photoReference,
            "maxWidth" to maxWidth,
            "maxHeight" to maxHeight
        )
    }

    // ======= 輔助工具和元數據相關 =======

    @Tool(description = "Get available place types with descriptions")
    fun getAvailablePlaceTypes(): String = executeWithErrorHandling {
        val placeTypes = PlaceType.values().map { type ->
            mapOf(
                "name" to type.name,
                "description" to getPlaceTypeDescription(type)
            )
        }

        mapOf(
            "placeTypes" to placeTypes,
            "count" to placeTypes.size
        )
    }

    @Tool(description = "Calculate distance and duration between two locations")
    fun calculateDistance(
        @ToolArg(description = "Origin latitude") originLat: Double,
        @ToolArg(description = "Origin longitude") originLng: Double,
        @ToolArg(description = "Destination latitude") destLat: Double,
        @ToolArg(description = "Destination longitude") destLng: Double,
        @ToolArg(description = "Travel mode (DRIVING, WALKING, BICYCLING, TRANSIT)") mode: String = "DRIVING"
    ): String = executeWithErrorHandling(
        mapOf(
            "originLat" to originLat,
            "originLng" to originLng,
            "destLat" to destLat,
            "destLng" to destLng,
            "travelMode" to mode
        )
    ) {
        // 驗證旅行模式
        val travelMode = try {
            TravelMode.valueOf(mode.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("無效的出行模式: $mode。可用模式: ${TravelMode.values().joinToString()}")
        }

        // 準備座標
        val origin = LatLng(originLat, originLng)
        val destination = LatLng(destLat, destLng)

        // 發送請求
        val request = com.google.maps.DistanceMatrixApi.newRequest(geoApiContext)
            .origins(origin)
            .destinations(destination)
            .mode(travelMode)
            .language(DEFAULT_LANGUAGE)

        val response = request.await()

        // 檢查響應是否有效
        if (response.rows.isEmpty() || response.rows[0].elements.isEmpty()) {
            throw IllegalStateException("找不到指定位置間的路線")
        }

        // 獲取行程元素
        val element = response.rows[0].elements[0]

        // 檢查行程狀態
        if (element.status != com.google.maps.model.DistanceMatrixElementStatus.OK) {
            throw IllegalStateException("無法計算距離: ${element.status}")
        }

        // 構建成功響應
        mapOf(
            "distance" to mapOf(
                "value" to element.distance.inMeters,
                "text" to element.distance.humanReadable
            ),
            "duration" to mapOf(
                "value" to element.duration.inSeconds,
                "text" to element.duration.humanReadable
            ),
            "origin" to mapOf("lat" to originLat, "lng" to originLng),
            "destination" to mapOf("lat" to destLat, "lng" to destLng),
            "travelMode" to travelMode.name
        )
    }

    // 地點類型描述映射
    private fun getPlaceTypeDescription(placeType: PlaceType): String {
        return when (placeType) {
            PlaceType.ACCOUNTING -> "會計服務"
            PlaceType.AIRPORT -> "機場"
            PlaceType.AMUSEMENT_PARK -> "遊樂園"
            PlaceType.AQUARIUM -> "水族館"
            PlaceType.ART_GALLERY -> "藝術畫廊"
            PlaceType.ATM -> "自動提款機"
            PlaceType.BAKERY -> "麵包店"
            PlaceType.BANK -> "銀行"
            PlaceType.BAR -> "酒吧"
            PlaceType.BEAUTY_SALON -> "美容院"
            PlaceType.BICYCLE_STORE -> "自行車店"
            PlaceType.BOOK_STORE -> "書店"
            PlaceType.BOWLING_ALLEY -> "保齡球館"
            PlaceType.BUS_STATION -> "公車站"
            PlaceType.CAFE -> "咖啡廳"
            PlaceType.CAMPGROUND -> "露營地"
            PlaceType.CAR_DEALER -> "汽車經銷商"
            PlaceType.CAR_RENTAL -> "租車服務"
            PlaceType.CAR_REPAIR -> "汽車修理"
            PlaceType.CAR_WASH -> "洗車服務"
            PlaceType.CASINO -> "賭場"
            PlaceType.CEMETERY -> "墓園"
            PlaceType.CHURCH -> "教堂"
            PlaceType.CITY_HALL -> "市政廳"
            PlaceType.CLOTHING_STORE -> "服裝店"
            PlaceType.CONVENIENCE_STORE -> "便利商店"
            PlaceType.COURTHOUSE -> "法院"
            PlaceType.DENTIST -> "牙醫"
            PlaceType.DEPARTMENT_STORE -> "百貨公司"
            PlaceType.DOCTOR -> "醫生"
            PlaceType.DRUGSTORE -> "藥店"
            PlaceType.ELECTRICIAN -> "電工"
            PlaceType.ELECTRONICS_STORE -> "電子產品商店"
            PlaceType.EMBASSY -> "大使館"
            PlaceType.FIRE_STATION -> "消防局"
            PlaceType.FLORIST -> "花店"
            PlaceType.FUNERAL_HOME -> "殯儀館"
            PlaceType.FURNITURE_STORE -> "家具店"
            PlaceType.GAS_STATION -> "加油站"
            PlaceType.GYM -> "健身房"
            PlaceType.HAIR_CARE -> "美髮沙龍"
            PlaceType.HARDWARE_STORE -> "五金店"
            PlaceType.HINDU_TEMPLE -> "印度廟"
            PlaceType.HOME_GOODS_STORE -> "家居用品店"
            PlaceType.HOSPITAL -> "醫院"
            PlaceType.INSURANCE_AGENCY -> "保險公司"
            PlaceType.JEWELRY_STORE -> "珠寶店"
            PlaceType.LAUNDRY -> "洗衣店"
            PlaceType.LAWYER -> "律師事務所"
            PlaceType.LIBRARY -> "圖書館"
            PlaceType.LIGHT_RAIL_STATION -> "輕軌站"
            PlaceType.LIQUOR_STORE -> "酒類商店"
            PlaceType.LOCAL_GOVERNMENT_OFFICE -> "地方政府辦公室"
            PlaceType.LOCKSMITH -> "鎖匠"
            PlaceType.LODGING -> "住宿"
            PlaceType.MEAL_DELIVERY -> "餐飲外送"
            PlaceType.MEAL_TAKEAWAY -> "餐飲外帶"
            PlaceType.MOSQUE -> "清真寺"
            PlaceType.MOVIE_RENTAL -> "電影租賃"
            PlaceType.MOVIE_THEATER -> "電影院"
            PlaceType.MOVING_COMPANY -> "搬家公司"
            PlaceType.MUSEUM -> "博物館"
            PlaceType.NIGHT_CLUB -> "夜店"
            PlaceType.PAINTER -> "油漆工"
            PlaceType.PARK -> "公園"
            PlaceType.PARKING -> "停車場"
            PlaceType.PET_STORE -> "寵物店"
            PlaceType.PHARMACY -> "藥局"
            PlaceType.PHYSIOTHERAPIST -> "物理治療師"
            PlaceType.PLUMBER -> "水管工"
            PlaceType.POLICE -> "警察局"
            PlaceType.POST_OFFICE -> "郵局"
            PlaceType.PRIMARY_SCHOOL -> "小學"
            PlaceType.REAL_ESTATE_AGENCY -> "房地產經紀公司"
            PlaceType.RESTAURANT -> "餐廳"
            PlaceType.ROOFING_CONTRACTOR -> "屋頂承包商"
            PlaceType.RV_PARK -> "露營車公園"
            PlaceType.SCHOOL -> "學校"
            PlaceType.SECONDARY_SCHOOL -> "中學"
            PlaceType.SHOE_STORE -> "鞋店"
            PlaceType.SHOPPING_MALL -> "購物中心"
            PlaceType.SPA -> "水療中心"
            PlaceType.STADIUM -> "體育場"
            PlaceType.STORAGE -> "倉儲"
            PlaceType.STORE -> "商店"
            PlaceType.SUBWAY_STATION -> "地鐵站"
            PlaceType.SUPERMARKET -> "超市"
            PlaceType.SYNAGOGUE -> "猶太教堂"
            PlaceType.TAXI_STAND -> "計程車站"
            PlaceType.TOURIST_ATTRACTION -> "旅遊景點"
            PlaceType.TRAIN_STATION -> "火車站"
            PlaceType.TRANSIT_STATION -> "交通站"
            PlaceType.TRAVEL_AGENCY -> "旅行社"
            PlaceType.UNIVERSITY -> "大學"
            PlaceType.VETERINARY_CARE -> "獸醫"
            PlaceType.ZOO -> "動物園"
            else -> "其他類型"
        }
    }
}
