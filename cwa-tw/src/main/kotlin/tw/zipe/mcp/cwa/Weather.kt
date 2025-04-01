package tw.zipe.mcp.cwa

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkiverse.mcp.server.Tool
import io.quarkiverse.mcp.server.ToolArg
import jakarta.enterprise.context.ApplicationScoped
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.eclipse.microprofile.rest.client.inject.RestClient

@ApplicationScoped
class Weather(
    @RestClient
    private val weatherClient: WeatherClient
) {

    private val authKey = System.getenv("AUTH_KEY")
    private val objectMapper = ObjectMapper()

    @Tool(description = "Get API ID corresponding to Taiwan cities/counties")
    fun getCityWeatherForecastApiId(@ToolArg(description = "City name,For example:花蓮縣、臺東縣") cityName: String): String? {
        val cityIdMap = mapOf(
            "臺北市" to "F-D0047-061",
            "新北市" to "F-D0047-069",
            "桃園市" to "F-D0047-005",
            "臺中市" to "F-D0047-073",
            "臺南市" to "F-D0047-077",
            "高雄市" to "F-D0047-065",
            "基隆市" to "F-D0047-049",
            "新竹縣" to "F-D0047-009",
            "新竹市" to "F-D0047-053",
            "苗栗縣" to "F-D0047-013",
            "彰化縣" to "F-D0047-017",
            "南投縣" to "F-D0047-021",
            "雲林縣" to "F-D0047-025",
            "嘉義縣" to "F-D0047-029",
            "嘉義市" to "F-D0047-057",
            "屏東縣" to "F-D0047-033",
            "宜蘭縣" to "F-D0047-001",
            "花蓮縣" to "F-D0047-041",
            "臺東縣" to "F-D0047-039",
            "澎湖縣" to "F-D0047-045",
            "金門縣" to "F-D0047-085",
            "連江縣" to "F-D0047-081"
        )
        return cityIdMap[cityName]
    }

    @Tool(description = "Get weather forecast data for all townships in Taiwan. LocationName is required. TimeFrom must be before TimeTo, and TimeTo cannot exceed TimeFrom by more than 24 hours.")
    fun getTownshipWeatherForecast(
        @ToolArg(description = "API ID corresponding to Taiwan county/city") locationId: String,
        @ToolArg(description = "Township name, e.g., 信義區, 萬華區") locationName: String,
        @ToolArg(description = "Start time, format: yyyy-MM-ddThh:mm:ss") timeFrom: String? = null,
        @ToolArg(description = "End time, format: yyyy-MM-ddThh:mm:ss") timeTo: String? = null
    ): JsonNode {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val now = LocalDateTime.now()

        val (startTime, endTime) = when {
            timeFrom == null && timeTo == null -> {
                Pair(now.format(formatter), now.plusDays(1).format(formatter))
            }

            timeFrom != null && timeTo != null -> {
                val parsedTimeFrom = LocalDateTime.parse(timeFrom, formatter)
                val parsedTimeTo = LocalDateTime.parse(timeTo, formatter)

                require(!parsedTimeTo.isBefore(parsedTimeFrom)) { "timeTo 不得在 timeFrom 之前" }

                val maxEndTime = parsedTimeFrom.plusHours(24)
                val actualEndTime = if (parsedTimeTo.isAfter(maxEndTime)) {
                    maxEndTime.format(formatter)
                } else {
                    timeTo
                }

                Pair(timeFrom, actualEndTime)
            }

            timeFrom != null -> {
                val parsedTimeFrom = LocalDateTime.parse(timeFrom, formatter)
                Pair(timeFrom, parsedTimeFrom.plusHours(24).format(formatter))
            }

            else -> {
                throw IllegalArgumentException("如果提供 timeTo，则必须同时提供 timeFrom")
            }
        }

        val response = weatherClient.getTownshipWeatherForecast(
            authorization = authKey,
            locationName = listOf(locationName),
            locationId = locationId,
            timeFrom = startTime,
            timeTo = endTime,
            sort = "time",
            format = "JSON",
            elementName = listOf(
                "天氣預報綜合描述",
                "3小時降雨機率",
                "溫度",
                "體感溫度",
                "最高溫度",
                "最低溫度"
            ).joinToString(",")
        )
        return extractRecords(response)
    }

    @Tool(description = "Get earthquake observation data for a specific area, areaName is required, returns data within 36 hours by default")
    fun getEarthquakeData(
        @ToolArg(description = "City name,For example:花蓮縣、臺東縣") areaName: String,
        @ToolArg(description = "Start time (yyyy-MM-ddThh:mm:ss)") timeFrom: String? = null,
        @ToolArg(description = "End time (yyyy-MM-ddThh:mm:ss)") timeTo: String? = null
    ): JsonNode {
        val now = LocalDateTime.now()
        val thirtySixHoursAgo = now.minusHours(36)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        val (startTime, endTime) = when {
            timeFrom == null && timeTo == null -> {
                // 預設情況：取過去36小時的數據
                Pair(thirtySixHoursAgo.format(formatter), now.format(formatter))
            }

            timeFrom != null && timeTo != null -> {
                // 兩個時間點都存在
                val parsedTimeFrom = LocalDateTime.parse(timeFrom, formatter)
                val parsedTimeTo = LocalDateTime.parse(timeTo, formatter)

                require(!parsedTimeTo.isBefore(parsedTimeFrom)) { "timeTo 不得在 timeFrom 之前" }

                val duration = Duration.between(parsedTimeFrom, parsedTimeTo)
                if (duration.toHours() > 36) {
                    // 如果時間範圍超過36小時，則限制為36小時
                    Pair(timeFrom, parsedTimeFrom.plusHours(36).format(formatter))
                } else {
                    Pair(timeFrom, timeTo)
                }
            }

            timeFrom != null -> {
                // 只有起始時間
                val parsedTimeFrom = LocalDateTime.parse(timeFrom, formatter)
                val timeFromPlus36Hours = parsedTimeFrom.plusHours(36)

                val actualEndTime = if (timeFromPlus36Hours.isAfter(now)) {
                    now.format(formatter)
                } else {
                    timeFromPlus36Hours.format(formatter)
                }

                Pair(timeFrom, actualEndTime)
            }

            else -> {
                // 只有結束時間
                val parsedTimeTo = LocalDateTime.parse(timeTo!!, formatter)
                Pair(parsedTimeTo.minusHours(36).format(formatter), timeTo)
            }
        }

        val response = weatherClient.getEarthquakeData(
            authorization = authKey,
            areaName = listOf(areaName),
            timeFrom = startTime,
            timeTo = endTime,
            limit = 1,
            sort = "time"
        )
        return extractRecords(response)
    }

    private fun extractRecords(response: Map<String, Any>): JsonNode {
        val jsonString = objectMapper.writeValueAsString(response)
        val rootNode = objectMapper.readTree(jsonString)
        return rootNode["records"]
    }
}
