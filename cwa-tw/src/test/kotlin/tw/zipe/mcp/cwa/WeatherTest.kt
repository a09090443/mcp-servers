package tw.zipe.mcp.cwa

import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@QuarkusTest
class WeatherTest {

    @Inject
    lateinit var weather: Weather

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    @Test
    fun testGetCityWeatherForecastApiId() {
        // 測試有效城市名稱
        val taipeId = weather.getCityWeatherForecastApiId("臺北市")
        assertEquals("F-D0047-061", taipeId)

        val hualienId = weather.getCityWeatherForecastApiId("花蓮縣")
        assertEquals("F-D0047-041", hualienId)

        val taichungId = weather.getCityWeatherForecastApiId("臺中市")
        assertEquals("F-D0047-073", taichungId)

        // 測試無效城市名稱
        val invalidId = weather.getCityWeatherForecastApiId("不存在的城市")
        assertNull(invalidId)
    }

    @Test
    fun testGetTownshipWeatherForecast() {
        // 測試基本功能（不傳時間參數）
        val result1 = weather.getTownshipWeatherForecast(
            locationId = "F-D0047-061",
            locationName = "萬華區"
        )
        assertNotNull(result1)
        assertTrue(result1.isObject || result1.isArray, "Result should be a JSON object or array")

        // 測試帶有明確時間範圍參數
        val now = LocalDateTime.now()
        val tomorrow = now.plusDays(1)
        val timeFrom = now.format(formatter)
        val timeTo = tomorrow.format(formatter)

        val result2 = weather.getTownshipWeatherForecast(
            locationId = "F-D0047-041",
            locationName = "花蓮市",
            timeFrom = timeFrom,
            timeTo = timeTo
        )
        assertNotNull(result2)
        assertTrue(result2.isObject || result2.isArray, "Result should be a JSON object or array")

        // 只提供開始時間的情況
        val result3 = weather.getTownshipWeatherForecast(
            locationId = "F-D0047-069",
            locationName = "板橋區",
            timeFrom = timeFrom
        )
        assertNotNull(result3)
        assertTrue(result3.isObject || result3.isArray, "Result should be a JSON object or array")
    }

    @Test
    fun testGetTownshipWeatherForecastTimeValidation() {
        val now = LocalDateTime.now()
        val tomorrow = now.plusDays(1)
        val dayAfterTomorrow = now.plusDays(2)
        val timeFrom = tomorrow.format(formatter)
        val timeTo = now.format(formatter)

        // 測試結束時間早於開始時間的情況
        val exception1 = assertThrows(IllegalArgumentException::class.java) {
            weather.getTownshipWeatherForecast(
                locationId = "F-D0047-061",
                locationName = "萬華區",
                timeFrom = timeFrom,
                timeTo = timeTo
            )
        }
        assertTrue(exception1.message?.contains("timeTo 不得在 timeFrom 之前") ?: false)

        // 測試時間範圍超過24小時的情況（應自動調整）
        val timeFromNormal = now.format(formatter)
        val timeToExcessive = dayAfterTomorrow.format(formatter)
        val result = weather.getTownshipWeatherForecast(
            locationId = "F-D0047-061",
            locationName = "信義區",
            timeFrom = timeFromNormal,
            timeTo = timeToExcessive
        )

        assertNotNull(result)
        assertTrue(result.isObject || result.isArray, "Result should be a JSON object or array")

        // 測試未來時間段
        val futureDateResult = weather.getTownshipWeatherForecast(
            locationId = "F-D0047-061",
            locationName = "信義區",
            timeFrom = "2025-04-01T00:00:00",
            timeTo = "2025-04-01T23:59:59"
        )

        assertNotNull(futureDateResult)
        assertTrue(futureDateResult.isObject || futureDateResult.isArray, "Result should be a JSON object or array")
    }

    @Test
    fun testGetEarthquakeData() {
        // 預設無參數測試（應返回過去36小時數據）
        val result1 = weather.getEarthquakeData("花蓮縣")
        assertNotNull(result1)
        assertTrue(result1.isObject || result1.isArray, "Result should be a JSON object or array")

        // 測試帶完整時間範圍
        val now = LocalDateTime.now()
        val yesterday = now.minusDays(1)
        val timeFrom = yesterday.format(formatter)
        val timeTo = now.format(formatter)

        val result2 = weather.getEarthquakeData(
            areaName = "花蓮縣",
            timeFrom = timeFrom,
            timeTo = timeTo
        )
        assertNotNull(result2)
        assertTrue(result2.isObject || result2.isArray, "Result should be a JSON object or array")

        // 只提供結束時間的測試
        val result3 = weather.getEarthquakeData(
            areaName = "花蓮縣",
            timeTo = timeTo
        )
        assertNotNull(result3)
        assertTrue(result3.isObject || result3.isArray, "Result should be a JSON object or array")

        // 只提供開始時間的測試
        val result4 = weather.getEarthquakeData(
            areaName = "花蓮縣",
            timeFrom = timeFrom
        )
        assertNotNull(result4)
        assertTrue(result4.isObject || result4.isArray, "Result should be a JSON object or array")

        // 測試其他區域
        val result5 = weather.getEarthquakeData("臺東縣")
        assertNotNull(result5)
        assertTrue(result5.isObject || result5.isArray, "Result should be a JSON object or array")
    }

    @Test
    fun testEarthquakeDataWithInvalidTimeRange() {
        val now = LocalDateTime.now()
        val tomorrow = now.plusDays(1)
        val timeFrom = tomorrow.format(formatter)
        val timeTo = now.format(formatter)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            weather.getEarthquakeData(
                areaName = "花蓮縣",
                timeFrom = timeFrom,
                timeTo = timeTo
            )
        }

        assertTrue(exception.message?.contains("timeTo 不得在 timeFrom 之前") ?: false)
    }

    @Test
    fun testGetEarthquakeDataTimeRangeAdjustment() {
        // 測試超過36小時的時間範圍（應自動調整）
        val now = LocalDateTime.now()
        val threeDaysAgo = now.minusDays(3)
        val timeFrom = threeDaysAgo.format(formatter)
        val timeTo = now.format(formatter)

        val result = weather.getEarthquakeData(
            areaName = "花蓮縣",
            timeFrom = timeFrom,
            timeTo = timeTo
        )

        assertNotNull(result)
        assertTrue(result.isObject || result.isArray, "Result should be a JSON object or array")
    }

}
