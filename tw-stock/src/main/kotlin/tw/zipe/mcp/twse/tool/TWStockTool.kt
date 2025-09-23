package tw.zipe.mcp.twse.tool

import io.quarkiverse.mcp.server.Tool
import io.quarkiverse.mcp.server.ToolArg
import jakarta.enterprise.context.ApplicationScoped
import kotlin.collections.ifEmpty
import kotlin.sequences.ifEmpty
import kotlin.text.get
import org.eclipse.microprofile.rest.client.inject.RestClient
import tw.zipe.mcp.twse.TWStockClient
import tw.zipe.mcp.twse.enumerate.IndustryCategory

private const val CODE_KEY: String = "公司代號"

@ApplicationScoped
class TWStock {

    @RestClient
    lateinit var twStockClient: TWStockClient

    // 公司基礎與財務資訊
    @Tool(description = "Obtain the basic information of a listed company as a JSON object based on its stock code")
    fun getCompanyBasicInfo(
        @ToolArg(
            description = "Stock code",
            required = true
        ) code: String?
    ): List<Map<String, Any>> {
        val data = twStockClient.getCompanyBasicInfo()
        return data.filter { it[CODE_KEY] == code }.ifEmpty {
            if (!code.isNullOrBlank()) {
                emptyList()
            } else data
        }
    }

    @Tool(description = "Obtain the daily major announcements of a company as a JSON object based on its stock code")
    fun getCompanyDailyAnnouncements(
        @ToolArg(
            description = "Stock code",
            required = true
        ) code: String?
    ): List<Map<String, Any>> {
        val data = twStockClient.getCompanyDailyAnnouncements()
        return data.filter { it[CODE_KEY] == code }.ifEmpty {
            if (!code.isNullOrBlank()) {
                emptyList()
            } else data
        }
    }

    @Tool(description = "Obtain the monthly revenue information of a company as a JSON object based on its stock code")
    fun getCompanyMonthlyRevenue(
        @ToolArg(
            description = "Stock code",
            required = true
        ) code: String?
    ): List<Map<String, Any>> {
        val data = twStockClient.getCompanyMonthlyRevenue()
        return data.filter { it[CODE_KEY] == code }.ifEmpty {
            if (!code.isNullOrBlank()) {
                emptyList()
            } else data
        }
    }

    @Tool(description = "Obtain the dividend information of a company as a JSON object based on its stock code")
    fun getCompanyDividendInfo(
        @ToolArg(
            description = "Stock code",
            required = false
        ) code: String?
    ): List<Map<String, Any>> {
        val data = twStockClient.getCompanyDividendInfo()
        return data.filter { it[CODE_KEY] == code }.ifEmpty {
            if (!code.isNullOrBlank()) {
                emptyList()
            } else data
        }
    }

    // 董監與股權資訊
    @Tool(description = "Obtain the shareholding structure information of a company as a JSON object based on its stock code")
    fun getCompanyIncomeStatement(
        @ToolArg(description = "Stock code", required = true) code: String?
    ): Map<String, Any> {
        val companyName = twStockClient.getCompanyBasicInfo()
            .firstOrNull { it[CODE_KEY] == code }
            ?.get("公司名稱")?.toString() ?: return emptyMap()
        val category = IndustryCategory.findByKeyWord(companyName)
        val data = twStockClient.getCompanyShareholdingInfo(category.name.lowercase())
        return data.firstOrNull { it[CODE_KEY] == code } ?: emptyMap()
    }

    // 資產負債表相關
    @Tool(description = "Obtain the balance sheet of a banking company as a JSON object based on its stock code")
    fun getCompanyBalanceSheet(@ToolArg(description = "Stock code") code: String): Map<String, Any> {
        val companyName = twStockClient.getCompanyBasicInfo()
            .firstOrNull { it[CODE_KEY] == code }
            ?.get("公司名稱")?.toString() ?: return emptyMap()
        val category = IndustryCategory.findByKeyWord(companyName)
        val data = twStockClient.getCompanyBalanceSheet(category.name.lowercase())
        return data.firstOrNull { it[CODE_KEY] == code } ?: emptyMap()
    }

    // ESG 相關
    @Tool(description = "Obtain the ESG sustainability report information of a company as a JSON object based on its stock code")
    fun getCompanyESGSustainabilityReport(
        @ToolArg(
            description = "Stock code",
            required = true
        ) code: String?
    ): List<Map<String, Any>> {
        val data = twStockClient.getCompanyESGSustainabilityReport()
        return data.filter { it[CODE_KEY] == code }.ifEmpty {
            if (!code.isNullOrBlank()) {
                emptyList()
            } else data
        }
    }

    @Tool(description = "Obtain the ESG functional committee information of a company as a JSON object based on its stock code")
    fun getCompanyESGFunctionalCommittee(
        @ToolArg(
            description = "Stock code",
            required = true
        ) code: String?
    ): List<Map<String, Any>> {
        val data = twStockClient.getCompanyESGFunctionalCommittee()
        return data.filter { it[CODE_KEY] == code }.ifEmpty {
            if (!code.isNullOrBlank()) {
                emptyList()
            } else data
        }
    }

    @Tool(description = "Obtain the ESG supply chain management information of a company as a JSON object based on its stock code")
    fun getCompanyESGSupplyChain(
        @ToolArg(
            description = "Stock code",
            required = true
        ) code: String?
    ): List<Map<String, Any>> {
        val data = twStockClient.getCompanyESGSupplyChain()
        return data.filter { it[CODE_KEY] == code }.ifEmpty {
            if (!code.isNullOrBlank()) {
                emptyList()
            } else data
        }
    }

    @Tool(description = "Obtain the ESG other related information of a company as a JSON object based on its stock code")
    fun getCompanyESGOtherInfo(
        @ToolArg(
            description = "Stock code",
            required = true
        ) code: String?
    ): List<Map<String, Any>> {
        val data = twStockClient.getCompanyESGOtherInfo()
        return data.filter { it[CODE_KEY] == code }.ifEmpty {
            if (!code.isNullOrBlank()) {
                emptyList()
            } else data
        }
    }

    @Tool(description = "Obtain the ESG risk management information of a company as a JSON object based on its stock code")
    fun getCompanyESGRiskManagement(
        @ToolArg(
            description = "Stock code",
            required = true
        ) code: String?
    ): List<Map<String, Any>> {
        val data = twStockClient.getCompanyESGRiskManagement()
        return data.filter { it[CODE_KEY] == code }.ifEmpty {
            if (!code.isNullOrBlank()) {
                emptyList()
            } else data
        }
    }

    // 上市權證基本資料彙總表
    //TODO 篩選公司名稱有問題
    @Tool(description = "Obtain the major announcements of a company as a JSON object based on its stock code")
    fun getCompanyAnnouncements(
        @ToolArg(
            description = "Stock code",
            required = true
        ) code: String?
    ): List<Map<String, Any>> {
        val data = twStockClient.getCompanyAnnouncements()
        return data.filter { it[CODE_KEY] == code }.ifEmpty {
            if (!code.isNullOrBlank()) {
                emptyList()
            } else data
        }
    }

    // 上市認購(售)權證每日成交資料檔
    //TODO 篩選公司名稱有問題
    @Tool(description = "Obtain the financial data of a company as a JSON object based on its stock code")
    fun getCompanyFinancialData(@ToolArg(description = "Stock code") code: String): Map<String, Any> {
        val data = twStockClient.getCompanyFinancialData()
        return data.firstOrNull { it[CODE_KEY] == code } ?: emptyMap()
    }

//    @Tool(description = "Obtain the business data of a company as a JSON object based on its stock code")
//    fun getCompanyBusinessData(@ToolArg(description = "Stock code") code: String): Map<String, Any> {
//        val data = twStockClient.getCompanyBusinessData()
//        return data.firstOrNull { it[CODE_KEY] == code } ?: emptyMap()
//    }

    // 市場整體資訊（不需要股票代碼參數）
    @Tool(description = "Obtain the news list as a JSON array")
    fun getNewsList(): List<Map<String, Any>> {
        return twStockClient.getNewsList()
    }

    @Tool(description = "Obtain the market events list as a JSON array")
    fun getEventList(): List<Map<String, Any>> {
        return twStockClient.getEventList()
    }

    @Tool(description = "Obtain the QFII categories information as a JSON array")
    fun getQFIICategories(): List<Map<String, Any>> {
        return twStockClient.getQFIICategories()
    }

    @Tool(description = "Obtain the QFII top 20 holdings as a JSON array")
    fun getTop20InvestorHoldingsSummary(): List<Map<String, Any>> {
        return twStockClient.getQFIITop20Holdings()
    }

    @Tool(description = "Obtain the market indices information as a JSON array")
    fun getMarketIndices(): List<Map<String, Any>> {
        return twStockClient.getMarketIndices()
    }

    //TODO 描述有問題
    @Tool(description = "Obtain the 5-minute historical data of indices as a JSON array")
    fun getIndices5MinHistory(): List<Map<String, Any>> {
        return twStockClient.getIndices5MinHistory()
    }

    @Tool(description = "Obtain the margin trading balance as a JSON array")
    fun getMarginTradingBalance(
        @ToolArg(
            description = "Stock code",
            required = true
        ) code: String?
    ): List<Map<String, Any>> {
        val data = twStockClient.getMarginTradingBalance()
        return data.filter { it["股票代號"] == code }.ifEmpty {
            if (!code.isNullOrBlank()) {
                emptyList()
            } else data
        }
    }

    @Tool(description = "Obtain the 5-minute real-time data of indices as a JSON array")
    fun getIndices5MinRealtime(): List<Map<String, Any>> {
        return twStockClient.getIndices5MinRealtime()
    }

    // 交易資訊
    @Tool(description = "Obtain the daily trading information of all stocks as a JSON array")
    fun getStockDailyInfo(@ToolArg(description = "Stock code", required = true) code: String?): List<Map<String, Any>> {
        val data = twStockClient.getStockDailyInfo()
        return data.filter { it["Code"] == code }.ifEmpty {
            if (!code.isNullOrBlank()) {
                emptyList()
            } else data
        }
    }

    @Tool(description = "Obtain the daily average prices of all stocks as a JSON array")
    fun getStockDailyAverage(
        @ToolArg(
            description = "Stock code",
            required = true
        ) code: String?
    ): List<Map<String, Any>> {
        val data = twStockClient.getStockDailyAverage()
        return data.filter { it["Code"] == code }.ifEmpty {
            if (!code.isNullOrBlank()) {
                emptyList()
            } else data
        }
    }

    @Tool(description = "Obtain the information of top 48 popular stocks as a JSON array")
    fun getTop48StocksInfo(
        @ToolArg(
            description = "Stock code",
            required = false
        ) code: String?
    ): List<Map<String, Any>> {
        val data = twStockClient.getTop48StocksInfo()
        return data.filter { it["Code"] == code }.ifEmpty {
            if (!code.isNullOrBlank()) {
                emptyList()
            } else data
        }
    }

    @Tool(description = "Obtain the ETF ranking as a JSON array")
    fun getETFRanking(): List<Map<String, Any>> {
        return twStockClient.getETFRanking()
    }

    @Tool(description = "Obtain the margin buying balance as a JSON array")
    fun getMarginBuyingBalance(): List<Map<String, Any>> {
        return twStockClient.getMarginBuyingBalance()
    }

    @Tool(description = "Obtain the short selling balance as a JSON array")
    fun getShortSellingBalance(): List<Map<String, Any>> {
        return twStockClient.getShortSellingBalance()
    }

    @Tool(description = "Obtain the valuation indicators as a JSON array")
    fun getStockValuationRatios(
        @ToolArg(description = "Stock code", required = false) code: String?,
        @ToolArg(description = "Dividend Yield Rate", defaultValue = "0.0", required = false) dividendYieldRate: Float?
    ): List<Map<String, Any>> {
        val data = twStockClient.getValuationIndicators()

        var filteredData = data

        // 如果有指定股票代碼，先過濾股票代碼
        if (!code.isNullOrBlank()) {
            return filteredData.filter { it["Code"] == code }
        } else if (dividendYieldRate != null) {
            filteredData = filteredData.filter { data ->
                val dividendYield = data["DividendYield"] as String
                dividendYield.toFloatOrNull()?.let { it > dividendYieldRate } ?: false
            }
        }

        return filteredData
    }

}
