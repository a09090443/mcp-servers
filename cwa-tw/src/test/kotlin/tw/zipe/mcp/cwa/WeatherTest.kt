package tw.zipe.mcp.cwa

import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@QuarkusTest
class WeatherIntegrationTest {

    @Inject
    lateinit var weather: Weather

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    @Test
    fun testGetWeatherForecast() {

        val result1 = weather.getWeatherForecast("臺北市")
        assertNotNull(result1)
        assertTrue(result1.isObject || result1.isArray, "Result should be a JSON object or array")

        // 带时间范围的测试
        val now = LocalDateTime.now()
        val tomorrow = now.plusDays(1)
        val timeFrom = now.format(formatter)
        val timeTo = tomorrow.format(formatter)

        val result2 = weather.getWeatherForecast(
            locationName = "臺北市",
            elementName = listOf("Wx", "MinT", "MaxT"),
            timeFrom = timeFrom,
            timeTo = timeTo
        )
        assertNotNull(result2)
        assertTrue(result2.isObject || result2.isArray, "Result should be a JSON object or array")
    }

    @Test
    fun testGetEarthquakeData() {
        // 不带可选参数的测试（默认36小时）
        val result1 = weather.getEarthquakeData("花蓮縣")
        assertNotNull(result1)
        assertTrue(result1.isObject || result1.isArray, "Result should be a JSON object or array")

        // 指定时间范围的测试
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
