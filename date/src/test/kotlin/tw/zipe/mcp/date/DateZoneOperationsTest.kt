package tw.zipe.mcp.date

import com.google.gson.Gson
import com.google.gson.JsonParser
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class DateZoneOperationsTest {

    private lateinit var dateZoneOperations: DateZoneOperations
    private lateinit var gson: Gson

    @BeforeEach
    fun setUp() {
        dateZoneOperations = DateZoneOperations()
        gson = Gson()
    }

    @Test
    fun testGetTodayDate() {
        val fixedDate = LocalDate.of(2025, 4, 10)
        val format = "yyyy-MM-dd"

        try {
            Mockito.mockStatic(LocalDate::class.java).use { mockedLocalDate ->
                mockedLocalDate.`when`<LocalDate> { LocalDate.now() }.thenReturn(fixedDate)

                val response = dateZoneOperations.getTodayDate(format)
                val jsonObject = JsonParser.parseString(response).asJsonObject

                assertTrue(jsonObject.get("success").asBoolean)
                assertEquals("2025-04-10", jsonObject.get("todayDate").asString)
                assertEquals(format, jsonObject.get("format").asString)
            }
        } catch (e: Exception) {
            fail("Exception should not be thrown: ${e.message}")
        }
    }

    @Test
    fun testGetDateTimeInTimeZone() {
        val fixedDateTime = LocalDateTime.of(2025, 4, 10, 15, 30, 0)
        val zoneId = ZoneId.of("Asia/Taipei")
        val zonedDateTime = ZonedDateTime.of(fixedDateTime, zoneId)

        try {
            Mockito.mockStatic(ZonedDateTime::class.java).use { mockedZonedDateTime ->
                mockedZonedDateTime.`when`<ZonedDateTime> { ZonedDateTime.now(zoneId) }.thenReturn(zonedDateTime)

                val response = dateZoneOperations.getDateTimeInTimeZone("Asia/Taipei", "yyyy-MM-dd HH:mm:ss")
                val jsonObject = JsonParser.parseString(response).asJsonObject

                assertTrue(jsonObject.get("success").asBoolean)
                assertEquals("2025-04-10 15:30:00", jsonObject.get("dateTime").asString)
                assertEquals("Asia/Taipei", jsonObject.get("timeZone").asString)
                assertEquals("yyyy-MM-dd HH:mm:ss", jsonObject.get("format").asString)
            }
        } catch (e: Exception) {
            fail("Exception should not be thrown: ${e.message}")
        }
    }

    @Test
    fun testConvertBetweenTimeZones() {
        val dateTimeString = "2025-04-10 15:30:00"
        val format = "yyyy-MM-dd HH:mm:ss"
        val sourceTimeZone = "Asia/Taipei"
        val targetTimeZone = "America/New_York"

        val response = dateZoneOperations.convertBetweenTimeZones(dateTimeString, format, sourceTimeZone, targetTimeZone)
        val jsonObject = JsonParser.parseString(response).asJsonObject

        assertTrue(jsonObject.get("success").asBoolean)
        assertEquals(dateTimeString, jsonObject.get("originalDateTime").asString)
        assertEquals(sourceTimeZone, jsonObject.get("originalTimeZone").asString)
        assertNotNull(jsonObject.get("convertedDateTime").asString)
        assertEquals(targetTimeZone, jsonObject.get("targetTimeZone").asString)
    }

    @Test
    fun testGetAvailableTimeZones() {
        val response = dateZoneOperations.getAvailableTimeZones()
        val jsonObject = JsonParser.parseString(response).asJsonObject

        assertTrue(jsonObject.get("success").asBoolean)
        assertTrue(jsonObject.get("availableTimeZones").asJsonArray.size() > 0)
        assertTrue(jsonObject.get("count").asInt > 0)
    }

    @Test
    fun testGetCommonTimeZonesByRegion() {
        val region = "Asia"
        val response = dateZoneOperations.getCommonTimeZonesByRegion(region)
        val jsonObject = JsonParser.parseString(response).asJsonObject

        assertTrue(jsonObject.get("success").asBoolean)
        assertEquals(region, jsonObject.get("region").asString)
        assertTrue(jsonObject.get("timeZones").asJsonArray.size() > 0)
        assertTrue(jsonObject.get("count").asInt > 0)
    }

    @Test
    fun testGetCommonTimeZonesByInvalidRegion() {
        val region = "InvalidRegion"
        val response = dateZoneOperations.getCommonTimeZonesByRegion(region)
        val jsonObject = JsonParser.parseString(response).asJsonObject

        assertFalse(jsonObject.get("success").asBoolean)
        assertTrue(jsonObject.get("error").asString.contains("No time zones found for region"))
    }

    @Test
    fun testIsToday() {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val todayString = today.format(formatter)

        val response = dateZoneOperations.isToday(todayString)
        val jsonObject = JsonParser.parseString(response).asJsonObject

        assertTrue(jsonObject.get("success").asBoolean)
        assertEquals(todayString, jsonObject.get("date").asString)
        assertTrue(jsonObject.get("isToday").asBoolean)
    }

    @Test
    fun testIsTodayWithPastDate() {
        val pastDate = LocalDate.now().minusDays(1)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val pastDateString = pastDate.format(formatter)

        val response = dateZoneOperations.isToday(pastDateString)
        val jsonObject = JsonParser.parseString(response).asJsonObject

        assertTrue(jsonObject.get("success").asBoolean)
        assertEquals(pastDateString, jsonObject.get("date").asString)
        assertFalse(jsonObject.get("isToday").asBoolean)
    }

    @Test
    fun testErrorHandling() {
        // Test with invalid format
        val response1 = dateZoneOperations.getTodayDate("invalid-format")
        val jsonObject1 = JsonParser.parseString(response1).asJsonObject
        assertFalse(jsonObject1.get("success").asBoolean)

        // Test with invalid time zone
        val response2 = dateZoneOperations.getDateTimeInTimeZone("Invalid/TimeZone")
        val jsonObject2 = JsonParser.parseString(response2).asJsonObject
        assertFalse(jsonObject2.get("success").asBoolean)

        // Test with invalid date format
        val response3 = dateZoneOperations.isToday("not-a-date")
        val jsonObject3 = JsonParser.parseString(response3).asJsonObject
        assertFalse(jsonObject3.get("success").asBoolean)
    }

    @Test
    fun testConvertBetweenTimeZonesWithInvalidInput() {
        // Test with invalid date time string
        val response1 = dateZoneOperations.convertBetweenTimeZones(
            "invalid-date-time",
            "yyyy-MM-dd HH:mm:ss",
            "Asia/Taipei",
            "America/New_York"
        )
        val jsonObject1 = JsonParser.parseString(response1).asJsonObject
        assertFalse(jsonObject1.get("success").asBoolean)

        // Test with invalid source time zone
        val response2 = dateZoneOperations.convertBetweenTimeZones(
            "2025-04-10 15:30:00",
            "yyyy-MM-dd HH:mm:ss",
            "Invalid/Zone",
            "America/New_York"
        )
        val jsonObject2 = JsonParser.parseString(response2).asJsonObject
        assertFalse(jsonObject2.get("success").asBoolean)
    }
}
