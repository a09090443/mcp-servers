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

    @Tool(description = "Get 36-hour weather forecast data for a city, locationName is required")
    fun getWeatherForecast(
        @ToolArg(description = "City name,For example:花蓮縣、臺東縣") locationName: String,
        @ToolArg(description = "Forecast factors, options include Wx (weather phenomenon), PoP (precipitation probability), MinT (minimum temperature), MaxT (maximum temperature), CI (comfort index), returns all by default") elementName: List<String>? = null,
        @ToolArg(description = "Start time of period, format: yyyy-MM-ddThh:mm:ss") timeFrom: String? = null,
        @ToolArg(description = "End time of period, format: yyyy-MM-ddThh:mm:ss") timeTo: String? = null
    ): JsonNode {
        val response = weatherClient.getWeatherForecast(
            authorization = authKey,
            locationName = listOf(locationName),
            elementName = listOf("Wx", "PoP", "MinT", "MaxT", "CI"),
            timeFrom = timeFrom.orEmpty(),
            timeTo = timeTo.orEmpty(),
            sort = "time"
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
