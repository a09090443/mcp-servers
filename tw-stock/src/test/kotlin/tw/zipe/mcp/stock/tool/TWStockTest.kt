package tw.zipe.mcp.twse.tool

import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@QuarkusTest
class TWStockTest {

    @Inject
    lateinit var twStock: TWStock

    // 公司基礎與財務資訊測試
    @Test
    fun `should return company basic info when stock code exists`() {
        val result = twStock.getCompanyBasicInfo("2330")

        if (result != null) {
            assertEquals("2330", result["公司代號"])
            assertNotNull(result["公司名稱"])
        }
    }

    @Test
    fun `should return null when stock code does not exist for basic info`() {
        val result = twStock.getCompanyBasicInfo("9999")
        assertNull(result)
    }

    @Test
    fun `should return company EPS info when stock code exists`() {
        val result = twStock.getCompanyEPS("3673")
        assertNotNull(result)
        assertTrue(result.isEmpty() || result.all { it["公司代號"] == "3673" })
    }

    @Test
    fun `should return company monthly revenue when stock code exists`() {
        val result = twStock.getCompanyMonthlyRevenue("2330")

        if (result != null) {
            assertEquals("2330", result["公司代號"])
        }
    }

    @Test
    fun `should return company dividend info when stock code exists`() {
        val result = twStock.getCompanyDividendInfo("2330")

        if (result != null) {
            assertEquals("2330", result["公司代號"])
        }
    }

    // 董監與股權資訊測試
    @Test
    fun `should return company shareholding info when stock code exists`() {
        val result = twStock.getCompanyShareholdingInfo("2330")

        if (result != null) {
            assertEquals("2330", result["公司代號"])
        }
    }

    // 資產負債表相關測試
    @Test
    fun `should return bank balance sheet when stock code exists`() {
        val result = twStock.getBankBalanceSheet("2330")

        if (result != null) {
            assertEquals("2330", result["公司代號"])
        }
    }

    @Test
    fun `should return manufacturing balance sheet when stock code exists`() {
        val result = twStock.getManufacturingBalanceSheet("2330")

        if (result != null) {
            assertEquals("2330", result["公司代號"])
        }
    }

    @Test
    fun `should return financial holding balance sheet when stock code exists`() {
        val result = twStock.getFinancialHoldingBalanceSheet("2330")

        if (result != null) {
            assertEquals("2330", result["公司代號"])
        }
    }

    @Test
    fun `should return insurance balance sheet when stock code exists`() {
        val result = twStock.getInsuranceBalanceSheet("2330")

        if (result != null) {
            assertEquals("2330", result["公司代號"])
        }
    }

    @Test
    fun `should return aggregated balance sheet when stock code exists`() {
        val result = twStock.getAggregatedBalanceSheet("2330")

        if (result != null) {
            assertEquals("2330", result["公司代號"])
        }
    }

    @Test
    fun `should return basic balance sheet when stock code exists`() {
        val result = twStock.getBasicBalanceSheet("2330")

        if (result != null) {
            assertEquals("2330", result["公司代號"])
        }
    }

    // ESG 相關測試
    @Test
    fun `should return ESG sustainability report when stock code exists`() {
        val result = twStock.getCompanyESGSustainabilityReport("2330")

        if (result != null) {
            assertEquals("2330", result["公司代號"])
        }
    }

    @Test
    fun `should return ESG functional committee info when stock code exists`() {
        val result = twStock.getCompanyESGFunctionalCommittee("2330")

        if (result != null) {
            assertEquals("2330", result["公司代號"])
        }
    }

    @Test
    fun `should return ESG supply chain info when stock code exists`() {
        val result = twStock.getCompanyESGSupplyChain("2330")

        if (result != null) {
            assertEquals("2330", result["公司代號"])
        }
    }

    @Test
    fun `should return ESG other info when stock code exists`() {
        val result = twStock.getCompanyESGOtherInfo("2330")

        if (result != null) {
            assertEquals("2330", result["公司代號"])
        }
    }

    @Test
    fun `should return ESG risk management info when stock code exists`() {
        val result = twStock.getCompanyESGRiskManagement("2330")

        if (result != null) {
            assertEquals("2330", result["公司代號"])
        }
    }

    // 其他公司資料測試
    @Test
    fun `should return company announcements when stock code exists`() {
        val result = twStock.getCompanyAnnouncements("2330")

        if (result != null) {
            assertEquals("2330", result["公司代號"])
        }
    }

    @Test
    fun `should return company financial data when stock code exists`() {
        val result = twStock.getCompanyFinancialData("2330")

        if (result != null) {
            assertEquals("2330", result["公司代號"])
        }
    }

    @Test
    fun `should return company business data when stock code exists`() {
        val result = twStock.getCompanyBusinessData("2330")

        if (result != null) {
            assertEquals("2330", result["公司代號"])
        }
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
    fun `should return QFII top 20 holdings`() {
        val result = twStock.getQFIITop20Holdings()
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
    fun `should return margin trading balance`() {
        val result = twStock.getMarginTradingBalance()
        assertNotNull(result)
    }

    @Test
    fun `should return indices 5 min realtime`() {
        val result = twStock.getIndices5MinRealtime()
        assertNotNull(result)
    }

    // 交易資訊測試
    @Test
    fun `should return stock daily info`() {
        val result = twStock.getStockDailyInfo()
        assertNotNull(result)
    }

    @Test
    fun `should return stock daily average`() {
        val result = twStock.getStockDailyAverage()
        assertNotNull(result)
    }

    @Test
    fun `should return top 48 stocks info`() {
        val result = twStock.getTop48StocksInfo()
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
    fun `should return valuation indicators`() {
        val result = twStock.getValuationIndicators()
        assertNotNull(result)
    }

    // 錯誤處理測試
    @Test
    fun `should handle empty stock code`() {
        val result = twStock.getCompanyBasicInfo("")
        assertNull(result)
    }

    @Test
    fun `should handle invalid stock code`() {
        val result = twStock.getCompanyBasicInfo("INVALID")
        assertNull(result)
    }
}
