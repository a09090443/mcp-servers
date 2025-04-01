package tw.zipe.mcp.cwa

        import io.quarkus.test.junit.QuarkusTest
        import jakarta.inject.Inject
        import java.time.LocalDateTime
        import java.time.format.DateTimeFormatter
        import org.junit.jupiter.api.Assertions.*
        import org.junit.jupiter.api.Test

        @QuarkusTest
        class WeatherTest {

            @Inject
            lateinit var weather: Weather

            private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

            @Test
            fun testGetCityWeatherForecastApiId() {
                // 测试有效城市名称
                val taipeId = weather.getCityWeatherForecastApiId("臺北市")
                assertEquals("F-D0047-061", taipeId)

                val hualienId = weather.getCityWeatherForecastApiId("花蓮縣")
                assertEquals("F-D0047-041", hualienId)

                val taichungId = weather.getCityWeatherForecastApiId("臺中市")
                assertEquals("F-D0047-073", taichungId)

                // 测试无效城市名称
                val invalidId = weather.getCityWeatherForecastApiId("不存在的城市")
                assertNull(invalidId)
            }

            @Test
            fun testGetTownshipWeatherForecast() {
                // 测试基本功能（不传时间参数）
                val result1 = weather.getTownshipWeatherForecast(
                    locationId = "F-D0047-061",
                    locationName = "萬華區"
                )
                assertNotNull(result1)
                assertTrue(result1.isObject || result1.isArray, "Result should be a JSON object or array")

                // 测试带有明确时间范围参数
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

                // 只提供开始时间的情况
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

                // 测试结束时间早于开始时间的情况
                val exception1 = assertThrows(IllegalArgumentException::class.java) {
                    weather.getTownshipWeatherForecast(
                        locationId = "F-D0047-061",
                        locationName = "萬華區",
                        timeFrom = timeFrom,
                        timeTo = timeTo
                    )
                }
                assertTrue(exception1.message?.contains("timeTo 不得在 timeFrom 之前") ?: false)

                // 测试只提供结束时间的情况
                val exception2 = assertThrows(IllegalArgumentException::class.java) {
                    weather.getTownshipWeatherForecast(
                        locationId = "F-D0047-061",
                        locationName = "信義區",
                        timeTo = timeTo
                    )
                }
                assertTrue(exception2.message?.contains("如果提供 timeTo，则必须同时提供 timeFrom") ?: false)

                // 测试时间范围超过24小时的情况（应自动调整）
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

                // 测试未来时间段
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
                // 默认无参数测试（应返回过去36小时数据）
                val result1 = weather.getEarthquakeData("花蓮縣")
                assertNotNull(result1)
                assertTrue(result1.isObject || result1.isArray, "Result should be a JSON object or array")

                // 测试带完整时间范围
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

                // 只提供结束时间的测试
                val result3 = weather.getEarthquakeData(
                    areaName = "花蓮縣",
                    timeTo = timeTo
                )
                assertNotNull(result3)
                assertTrue(result3.isObject || result3.isArray, "Result should be a JSON object or array")

                // 只提供开始时间的测试
                val result4 = weather.getEarthquakeData(
                    areaName = "花蓮縣",
                    timeFrom = timeFrom
                )
                assertNotNull(result4)
                assertTrue(result4.isObject || result4.isArray, "Result should be a JSON object or array")

                // 测试其他区域
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
                // 测试超过36小时的时间范围（应自动调整）
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
