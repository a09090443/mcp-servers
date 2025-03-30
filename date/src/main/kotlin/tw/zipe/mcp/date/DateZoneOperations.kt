package tw.zipe.mcp.date

import com.google.gson.Gson
import io.quarkiverse.mcp.server.Tool
import io.quarkiverse.mcp.server.ToolArg
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * @author Date Zone Operations
 * @created 2025/4/10
 */
class DateZoneOperations {
    private companion object {
        private val gson = Gson()
    }

    // Generate JSON success response
    private fun createSuccessResponse(data: Map<String, Any?>): String {
        val responseMap = mutableMapOf<String, Any?>("success" to true)
        responseMap.putAll(data)
        return gson.toJson(responseMap)
    }

    // Generate JSON error response
    private fun createErrorResponse(error: String, data: Map<String, Any?> = emptyMap()): String {
        val responseMap = mutableMapOf<String, Any?>("success" to false, "error" to error)
        responseMap.putAll(data)
        return gson.toJson(responseMap)
    }

    // Get today's date
    @Tool(description = "Get today's date")
    fun getTodayDate(
        @ToolArg(description = "Date format, default is yyyy-MM-dd") format: String = "yyyy-MM-dd"
    ): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern(format)
            val today = LocalDate.now().format(formatter)

            createSuccessResponse(
                mapOf(
                    "todayDate" to today,
                    "format" to format
                )
            )
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Failed to get today's date")
        }
    }

    // Get current date time in specified time zone
    @Tool(description = "Get current date time in specified time zone")
    fun getDateTimeInTimeZone(
        @ToolArg(description = "Time zone ID, e.g.: Asia/Taipei, America/New_York, Europe/London, UTC") timeZone: String,
        @ToolArg(description = "Date time format, default is yyyy-MM-dd HH:mm:ss") format: String = "yyyy-MM-dd HH:mm:ss"
    ): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern(format)
            val zoneId = ZoneId.of(timeZone)
            val zonedDateTime = ZonedDateTime.now(zoneId)
            val dateTimeInZone = zonedDateTime.format(formatter)

            createSuccessResponse(
                mapOf(
                    "dateTime" to dateTimeInZone,
                    "timeZone" to timeZone,
                    "format" to format,
                    "offsetHours" to zonedDateTime.offset.totalSeconds / 3600
                )
            )
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Failed to get time zone date time")
        }
    }

    // Convert date time between time zones
    @Tool(description = "Convert date time from one time zone to another")
    fun convertBetweenTimeZones(
        @ToolArg(description = "Date time string") dateTimeString: String,
        @ToolArg(description = "Source date time format") format: String = "yyyy-MM-dd HH:mm:ss",
        @ToolArg(description = "Source time zone ID, e.g.: Asia/Taipei") sourceTimeZone: String,
        @ToolArg(description = "Target time zone ID, e.g.: America/New_York") targetTimeZone: String
    ): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern(format)

            val sourceZoneId = ZoneId.of(sourceTimeZone)
            val targetZoneId = ZoneId.of(targetTimeZone)

            val localDateTime = LocalDateTime.parse(dateTimeString, formatter)
            val sourceZonedDateTime = ZonedDateTime.of(localDateTime, sourceZoneId)
            val targetZonedDateTime = sourceZonedDateTime.withZoneSameInstant(targetZoneId)

            val result = targetZonedDateTime.format(formatter)

            createSuccessResponse(
                mapOf(
                    "originalDateTime" to dateTimeString,
                    "originalTimeZone" to sourceTimeZone,
                    "convertedDateTime" to result,
                    "targetTimeZone" to targetTimeZone,
                    "sourceOffset" to sourceZonedDateTime.offset.toString(),
                    "targetOffset" to targetZonedDateTime.offset.toString()
                )
            )
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Failed to convert between time zones")
        }
    }

    // Get all available time zone IDs
    @Tool(description = "Get all available time zone IDs")
    fun getAvailableTimeZones(): String {
        return try {
            val availableZones = ZoneId.getAvailableZoneIds().sorted()

            createSuccessResponse(
                mapOf(
                    "availableTimeZones" to availableZones,
                    "count" to availableZones.size
                )
            )
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Failed to get available time zones")
        }
    }

    // Get common time zones by region
    @Tool(description = "Get common time zones by region")
    fun getCommonTimeZonesByRegion(
        @ToolArg(description = "Region name (Asia, Europe, America, Pacific, Australia, Africa)") region: String
    ): String {
        return try {
            val allZones = ZoneId.getAvailableZoneIds()
            val regionZones = allZones.filter { it.startsWith(region) }.sorted()

            if (regionZones.isEmpty()) {
                return createErrorResponse("No time zones found for region '$region'. Available regions: Asia, Europe, America, Pacific, Australia, Africa")
            }

            createSuccessResponse(
                mapOf(
                    "region" to region,
                    "timeZones" to regionZones,
                    "count" to regionZones.size
                )
            )
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Failed to get region time zones")
        }
    }

    // Check if date is today
    @Tool(description = "Check if given date is today")
    fun isToday(
        @ToolArg(description = "Date string (yyyy-MM-dd)") dateString: String
    ): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date = LocalDate.parse(dateString, formatter)
            val today = LocalDate.now()
            val isToday = date.isEqual(today)

            createSuccessResponse(
                mapOf(
                    "date" to dateString,
                    "isToday" to isToday,
                    "today" to today.format(formatter)
                )
            )
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Failed to check if date is today")
        }
    }
}
