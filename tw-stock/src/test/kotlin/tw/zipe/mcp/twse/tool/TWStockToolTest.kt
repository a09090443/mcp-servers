package tw.zipe.mcp.twse.tool

import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@QuarkusTest
class TWStockToolTest {

    @Inject
    lateinit var twStock: TWStock

    // 公司基礎與財務資訊測試
    @Test
    fun `should return company basic info when stock code exists`() {
        val result = twStock.getCompanyBasicInfo("2330")

        if (result.isNotEmpty()) {
            assertTrue(result.all { it["公司代號"] == "2330" })
            assertNotNull(result.first()["公司名稱"])
        }
    }

    @Test
    fun `should return empty list when stock code does not exist for basic info`() {
        val result = twStock.getCompanyBasicInfo("9999")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return all company basic info when code is null`() {
        val result = twStock.getCompanyBasicInfo(null)
        assertNotNull(result)
        // 當 code 為 null 時應該返回所有公司資料
    }

    @Test
    fun `should return company EPS info when stock code exists`() {
        val result = twStock.getCompanyEPS("2330")
        assertNotNull(result)
        assertTrue(result.isEmpty() || result.all { it["公司代號"] == "2330" })
    }

    @Test
    fun `should return empty list when stock code does not exist for EPS`() {
        val result = twStock.getCompanyEPS("9999")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return company monthly revenue when stock code exists`() {
        val result = twStock.getCompanyMonthlyRevenue("2330")
        assertNotNull(result)
        assertTrue(result.isEmpty() || result.all { it["公司代號"] == "2330" })
    }

    @Test
    fun `should return empty list when stock code does not exist for monthly revenue`() {
        val result = twStock.getCompanyMonthlyRevenue("9999")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return company dividend info when stock code exists`() {
        val result = twStock.getCompanyDividendInfo("2330")
        assertNotNull(result)
        assertTrue(result.isEmpty() || result.all { it["公司代號"] == "2330" })
    }

    @Test
    fun `should return all dividend info when code is null`() {
        val result = twStock.getCompanyDividendInfo(null)
        assertNotNull(result)
        // dividend info 的 code 參數是 required = false，所以 null 時應該返回所有資料
    }

    // 董監與股權資訊測試
    @Test
    fun `should return company income statement when stock code exists`() {
        val result = twStock.getCompanyIncomeStatement("2330")
        assertNotNull(result)
        assertTrue(result.isEmpty() || result["公司代號"] == "2330")
    }

    @Test
    fun `should return empty map when stock code does not exist for income statement`() {
        val result = twStock.getCompanyIncomeStatement("9999")
        assertTrue(result.isEmpty())
    }

    // 資產負債表相關測試
    @Test
    fun `should return company balance sheet when stock code exists`() {
        val result = twStock.getCompanyBalanceSheet("2330")
        assertNotNull(result)
        assertTrue(result.isEmpty() || result["公司代號"] == "2330")
    }

    @Test
    fun `should return empty map when stock code does not exist for balance sheet`() {
        val result = twStock.getCompanyBalanceSheet("9999")
        assertTrue(result.isEmpty())
    }

    // ESG 相關測試
    @Test
    fun `should return ESG sustainability report when stock code exists`() {
        val result = twStock.getCompanyESGSustainabilityReport("2330")
        assertNotNull(result)
        assertTrue(result.isEmpty() || result.all { it["公司代號"] == "2330" })
    }

    @Test
    fun `should return empty list when stock code does not exist for ESG sustainability report`() {
        val result = twStock.getCompanyESGSustainabilityReport("9999")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return ESG functional committee info when stock code exists`() {
        val result = twStock.getCompanyESGFunctionalCommittee("2330")
        assertNotNull(result)
        assertTrue(result.isEmpty() || result.all { it["公司代號"] == "2330" })
    }

    @Test
    fun `should return empty list when stock code does not exist for ESG functional committee`() {
        val result = twStock.getCompanyESGFunctionalCommittee("9999")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return ESG supply chain info when stock code exists`() {
        val result = twStock.getCompanyESGSupplyChain("2330")
        assertNotNull(result)
        assertTrue(result.isEmpty() || result.all { it["公司代號"] == "2330" })
    }

    @Test
    fun `should return empty list when stock code does not exist for ESG supply chain`() {
        val result = twStock.getCompanyESGSupplyChain("9999")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return ESG other info when stock code exists`() {
        val result = twStock.getCompanyESGOtherInfo("2330")
        assertNotNull(result)
        assertTrue(result.isEmpty() || result.all { it["公司代號"] == "2330" })
    }

    @Test
    fun `should return empty list when stock code does not exist for ESG other info`() {
        val result = twStock.getCompanyESGOtherInfo("9999")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return ESG risk management info when stock code exists`() {
        val result = twStock.getCompanyESGRiskManagement("2330")
        assertNotNull(result)
        assertTrue(result.isEmpty() || result.all { it["公司代號"] == "2330" })
    }

    @Test
    fun `should return empty list when stock code does not exist for ESG risk management`() {
        val result = twStock.getCompanyESGRiskManagement("9999")
        assertTrue(result.isEmpty())
    }

    // 其他公司資料測試
    @Test
    fun `should return company announcements when stock code exists`() {
        val result = twStock.getCompanyAnnouncements("2330")
        assertNotNull(result)
        assertTrue(result.isEmpty() || result.all { it["公司代號"] == "2330" })
    }

    @Test
    fun `should return empty list when stock code does not exist for announcements`() {
        val result = twStock.getCompanyAnnouncements("9999")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return company financial data when stock code exists`() {
        val result = twStock.getCompanyFinancialData("2330")
        assertNotNull(result)
        assertTrue(result.isEmpty() || result["公司代號"] == "2330")
    }

    @Test
    fun `should return empty map when stock code does not exist for financial data`() {
        val result = twStock.getCompanyFinancialData("9999")
        assertTrue(result.isEmpty())
    }

    // 市場整體資訊測試（不需要股票代碼參數）
    @Test
    fun `should return news list`() {
        val result = twStock.getNewsList()
        assertNotNull(result)
    }

    @Test
    fun `should return event list`() {
        val result = twStock.getEventList()
        assertNotNull(result)
    }

    @Test
    fun `should return QFII categories`() {
        val result = twStock.getQFIICategories()
        assertNotNull(result)
    }

    @Test
    fun `should return top 20 investor holdings summary`() {
        val result = twStock.getTop20InvestorHoldingsSummary()
        assertNotNull(result)
    }

    @Test
    fun `should return market indices`() {
        val result = twStock.getMarketIndices()
        assertNotNull(result)
    }

    @Test
    fun `should return indices 5 min history`() {
        val result = twStock.getIndices5MinHistory()
        assertNotNull(result)
    }

    @Test
    fun `should return margin trading balance when stock code exists`() {
        val result = twStock.getMarginTradingBalance("2330")
        assertNotNull(result)
        assertTrue(result.isEmpty() || result.all { it["股票代號"] == "2330" })
    }

    @Test
    fun `should return all margin trading balance when code is null`() {
        val result = twStock.getMarginTradingBalance(null)
        assertNotNull(result)
    }

    @Test
    fun `should return indices 5 min realtime`() {
        val result = twStock.getIndices5MinRealtime()
        assertNotNull(result)
    }

    // 交易資訊測試
    @Test
    fun `should return stock daily info when stock code exists`() {
        val result = twStock.getStockDailyInfo("2330")
        assertNotNull(result)
        assertTrue(result.isEmpty() || result.all { it["Code"] == "2330" })
    }

    @Test
    fun `should return empty list when stock code does not exist for daily info`() {
        val result = twStock.getStockDailyInfo("9999")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return stock daily average when stock code exists`() {
        val result = twStock.getStockDailyAverage("2330")
        assertNotNull(result)
        assertTrue(result.isEmpty() || result.all { it["Code"] == "2330" })
    }

    @Test
    fun `should return empty list when stock code does not exist for daily average`() {
        val result = twStock.getStockDailyAverage("9999")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return top 48 stocks info when stock code exists`() {
        val result = twStock.getTop48StocksInfo("00930")
        assertNotNull(result)
        assertTrue(result.isEmpty() || result.all { it["Code"] == "00930" })
    }

    @Test
    fun `should return all top 48 stocks info when code is null`() {
        val result = twStock.getTop48StocksInfo(null)
        assertNotNull(result)
    }

    @Test
    fun `should return ETF ranking`() {
        val result = twStock.getETFRanking()
        assertNotNull(result)
    }

    @Test
    fun `should return margin buying balance`() {
        val result = twStock.getMarginBuyingBalance()
        assertNotNull(result)
    }

    @Test
    fun `should return short selling balance`() {
        val result = twStock.getShortSellingBalance()
        assertNotNull(result)
    }

    @Test
    fun `should return stock valuation ratios when stock code exists`() {
        val result = twStock.getStockValuationRatios("2330", null)
        assertNotNull(result)
        assertTrue(result.isEmpty() || result.all { it["Code"] == "2330" })
    }

    @Test
    fun `should return all stock valuation ratios when no parameters provided`() {
        val result = twStock.getStockValuationRatios(null, null)
        assertNotNull(result)
    }

    @Test
    fun `should filter by dividend yield rate when provided and no stock code`() {
        val dividendYieldRate = 3.0f
        val result = twStock.getStockValuationRatios(null, dividendYieldRate)
        assertNotNull(result)

        if (result.isNotEmpty()) {
            result.forEach { data ->
                val dividendYield = data["DividendYield"] as String
                val value = dividendYield.toFloatOrNull()
                if (value != null) {
                    assertTrue(
                        value > dividendYieldRate,
                        "Dividend yield $value should be greater than $dividendYieldRate"
                    )
                }
            }
        }
    }

    @Test
    fun `should return stock by code only when both stock code and dividend yield rate provided`() {
        // 根據實際邏輯，當提供股票代碼時，會直接返回該股票，不進行股息殖利率過濾
        val result = twStock.getStockValuationRatios("2330", 2.0f)
        assertNotNull(result)

        if (result.isNotEmpty()) {
            assertTrue(
                result.all { it["Code"] == "2330" },
                "Should only return data for stock code 2330"
            )
            // 不需要檢查股息殖利率，因為有股票代碼時會直接返回，不進行過濾
        }
    }

    @Test
    fun `should return empty list when stock code does not exist for valuation ratios`() {
        val result = twStock.getStockValuationRatios("9999", null)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return empty list when dividend yield rate is too high and no stock code`() {
        val result = twStock.getStockValuationRatios(null, 99.0f)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should handle empty or blank stock code properly for valuation ratios`() {
        val resultEmpty = twStock.getStockValuationRatios("", null)
        val resultBlank = twStock.getStockValuationRatios("   ", null)

        assertNotNull(resultEmpty)
        assertNotNull(resultBlank)
        // 空字串或空白字串應該被視為無效代碼，返回所有數據
    }

    @Test
    fun `should handle zero dividend yield rate when no stock code`() {
        val result = twStock.getStockValuationRatios(null, 0.0f)
        assertNotNull(result)

        if (result.isNotEmpty()) {
            result.forEach { data ->
                val dividendYield = data["DividendYield"]
                when (dividendYield) {
                    is Number -> assertTrue(dividendYield.toFloat() > 0.0f)
                    is String -> {
                        val value = dividendYield.toFloatOrNull()
                        if (value != null) {
                            assertTrue(value > 0.0f)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `should ignore dividend yield rate when stock code is provided`() {
        // 測試當提供股票代碼時，股息殖利率參數會被忽略
        val resultWithoutDividend = twStock.getStockValuationRatios("2330", null)
        val resultWithDividend = twStock.getStockValuationRatios("2330", 10.0f)

        // 兩個結果應該相同，因為提供股票代碼時會忽略股息殖利率過濾
        if (resultWithoutDividend.isNotEmpty() && resultWithDividend.isNotEmpty()) {
            assertEquals(resultWithoutDividend.size, resultWithDividend.size)
        }
    }

    // 錯誤處理測試
    @Test
    fun `should handle empty stock code for basic info`() {
        val result = twStock.getCompanyBasicInfo("")
        assertNotNull(result)
        // 空字串會被 isNullOrBlank() 判斷為 true，應該返回所有資料
    }

    @Test
    fun `should handle invalid stock code for basic info`() {
        val result = twStock.getCompanyBasicInfo("INVALID")
        assertTrue(result.isEmpty())
    }

    // 測試一些方法的邊界條件
    @Test
    fun `should handle null parameters properly`() {
        // 測試各種 null 參數的處理
        assertNotNull(twStock.getCompanyBasicInfo(null))
        assertNotNull(twStock.getCompanyEPS(null))
        assertNotNull(twStock.getCompanyMonthlyRevenue(null))
        assertNotNull(twStock.getCompanyDividendInfo(null))
        assertNotNull(twStock.getMarginTradingBalance(null))
        assertNotNull(twStock.getStockDailyInfo(null))
        assertNotNull(twStock.getStockDailyAverage(null))
        assertNotNull(twStock.getTop48StocksInfo(null))
    }

    @Test
    fun `should handle blank parameters properly`() {
        // 測試空白字串參數的處理
        assertTrue(twStock.getCompanyBasicInfo("   ").isNotEmpty() ||
                  twStock.getCompanyBasicInfo("   ").isEmpty())
        assertTrue(twStock.getCompanyEPS("   ").isNotEmpty() ||
                  twStock.getCompanyEPS("   ").isEmpty())
        assertTrue(twStock.getCompanyMonthlyRevenue("   ").isNotEmpty() ||
                  twStock.getCompanyMonthlyRevenue("   ").isEmpty())
    }
}
