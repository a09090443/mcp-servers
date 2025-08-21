package tw.zipe.mcp.twse.tool

import io.quarkiverse.mcp.server.Tool
import io.quarkiverse.mcp.server.ToolArg
import org.eclipse.microprofile.rest.client.inject.RestClient
import tw.zipe.mcp.twse.TWStockClient

class TWStock {

    @RestClient
    lateinit var twStockClient: TWStockClient

    // 公司基礎與財務資訊
    @Tool(description = "根據股票代碼獲取上市公司基本資訊")
    fun getCompanyBasicInfo(@ToolArg(description = "股票代碼") code: String): Map<String, Any>? {
        val data = twStockClient.getCompanyBasicInfo()
        return data.firstOrNull { it["公司代號"] == code }
    }

    @Tool(description = "根據股票代碼獲取公司每股盈餘（EPS）資訊")
    fun getCompanyEPS(@ToolArg(description = "股票代碼") code: String): List<Map<String, Any>> {
        val data = twStockClient.getCompanyEPS()
        return data.filter { it["公司代號"] == code }
    }

    @Tool(description = "根據股票代碼獲取公司月營收資訊")
    fun getCompanyMonthlyRevenue(@ToolArg(description = "股票代碼") code: String): Map<String, Any>? {
        val data = twStockClient.getCompanyMonthlyRevenue()
        return data.firstOrNull { it["公司代號"] == code }
    }

    @Tool(description = "根據股票代碼獲取公司股利資訊")
    fun getCompanyDividendInfo(@ToolArg(description = "股票代碼") code: String): Map<String, Any>? {
        val data = twStockClient.getCompanyDividendInfo()
        return data.firstOrNull { it["公司代號"] == code }
    }

    // 董監與股權資訊
    @Tool(description = "根據股票代碼獲取公司股權結構資訊")
    fun getCompanyShareholdingInfo(@ToolArg(description = "股票代碼") code: String): Map<String, Any>? {
        val data = twStockClient.getCompanyShareholdingInfo()
        return data.firstOrNull { it["公司代號"] == code }
    }

    // 資產負債表相關
    @Tool(description = "根據股票代碼獲取銀行業資產負債表")
    fun getBankBalanceSheet(@ToolArg(description = "股票代碼") code: String): Map<String, Any>? {
        val data = twStockClient.getBankBalanceSheet()
        return data.firstOrNull { it["公司代號"] == code }
    }

    @Tool(description = "根據股票代碼獲取製造業資產負債表")
    fun getManufacturingBalanceSheet(@ToolArg(description = "股票代碼") code: String): Map<String, Any>? {
        val data = twStockClient.getManufacturingBalanceSheet()
        return data.firstOrNull { it["公司代號"] == code }
    }

    @Tool(description = "根據股票代碼獲取金控業資產負債表")
    fun getFinancialHoldingBalanceSheet(@ToolArg(description = "股票代碼") code: String): Map<String, Any>? {
        val data = twStockClient.getFinancialHoldingBalanceSheet()
        return data.firstOrNull { it["公司代號"] == code }
    }

    @Tool(description = "根據股票代碼獲取保險業資產負債表")
    fun getInsuranceBalanceSheet(@ToolArg(description = "股票代碼") code: String): Map<String, Any>? {
        val data = twStockClient.getInsuranceBalanceSheet()
        return data.firstOrNull { it["公司代號"] == code }
    }

    @Tool(description = "根據股票代碼獲取綜合資產負債表")
    fun getAggregatedBalanceSheet(@ToolArg(description = "股票代碼") code: String): Map<String, Any>? {
        val data = twStockClient.getAggregatedBalanceSheet()
        return data.firstOrNull { it["公司代號"] == code }
    }

    @Tool(description = "根據股票代碼獲取基礎資產負債表")
    fun getBasicBalanceSheet(@ToolArg(description = "股票代碼") code: String): Map<String, Any>? {
        val data = twStockClient.getBasicBalanceSheet()
        return data.firstOrNull { it["公司代號"] == code }
    }

    // ESG 相關
    @Tool(description = "根據股票代碼獲取公司ESG永續報告書資訊")
    fun getCompanyESGSustainabilityReport(@ToolArg(description = "股票代碼") code: String): Map<String, Any>? {
        val data = twStockClient.getCompanyESGSustainabilityReport()
        return data.firstOrNull { it["公司代號"] == code }
    }

    @Tool(description = "根據股票代碼獲取公司ESG功能性委員會資訊")
    fun getCompanyESGFunctionalCommittee(@ToolArg(description = "股票代碼") code: String): Map<String, Any>? {
        val data = twStockClient.getCompanyESGFunctionalCommittee()
        return data.firstOrNull { it["公司代號"] == code }
    }

    @Tool(description = "根據股票代碼獲取公司ESG供應鏈管理資訊")
    fun getCompanyESGSupplyChain(@ToolArg(description = "股票代碼") code: String): Map<String, Any>? {
        val data = twStockClient.getCompanyESGSupplyChain()
        return data.firstOrNull { it["公司代號"] == code }
    }

    @Tool(description = "根據股票代碼獲取公司ESG其他相關資訊")
    fun getCompanyESGOtherInfo(@ToolArg(description = "股票代碼") code: String): Map<String, Any>? {
        val data = twStockClient.getCompanyESGOtherInfo()
        return data.firstOrNull { it["公司代號"] == code }
    }

    @Tool(description = "根據股票代碼獲取公司ESG風險管理資訊")
    fun getCompanyESGRiskManagement(@ToolArg(description = "股票代碼") code: String): Map<String, Any>? {
        val data = twStockClient.getCompanyESGRiskManagement()
        return data.firstOrNull { it["公司代號"] == code }
    }

    // 其他公司資料
    @Tool(description = "根據股票代碼獲取公司重大訊息公告")
    fun getCompanyAnnouncements(@ToolArg(description = "股票代碼") code: String): Map<String, Any>? {
        val data = twStockClient.getCompanyAnnouncements()
        return data.firstOrNull { it["公司代號"] == code }
    }

    @Tool(description = "根據股票代碼獲取公司財務數據")
    fun getCompanyFinancialData(@ToolArg(description = "股票代碼") code: String): Map<String, Any>? {
        val data = twStockClient.getCompanyFinancialData()
        return data.firstOrNull { it["公司代號"] == code }
    }

    @Tool(description = "根據股票代碼獲取公司營業數據")
    fun getCompanyBusinessData(@ToolArg(description = "股票代碼") code: String): Map<String, Any>? {
        val data = twStockClient.getCompanyBusinessData()
        return data.firstOrNull { it["公司代號"] == code }
    }

    // 市場整體資訊（不需要股票代碼參數）
    @Tool(description = "獲取新聞列表")
    fun getNewsList(): List<Map<String, Any>> {
        return twStockClient.getNewsList()
    }

    @Tool(description = "獲取市場事件列表")
    fun getEventList(): List<Map<String, Any>> {
        return twStockClient.getEventList()
    }

    @Tool(description = "獲取QFII分類資訊")
    fun getQFIICategories(): List<Map<String, Any>> {
        return twStockClient.getQFIICategories()
    }

    @Tool(description = "獲取QFII前20大持股")
    fun getQFIITop20Holdings(): List<Map<String, Any>> {
        return twStockClient.getQFIITop20Holdings()
    }

    @Tool(description = "獲取市場指數資訊")
    fun getMarketIndices(): List<Map<String, Any>> {
        return twStockClient.getMarketIndices()
    }

    @Tool(description = "獲取指數5分鐘歷史資料")
    fun getIndices5MinHistory(): List<Map<String, Any>> {
        return twStockClient.getIndices5MinHistory()
    }

    @Tool(description = "獲取融資融券餘額")
    fun getMarginTradingBalance(): List<Map<String, Any>> {
        return twStockClient.getMarginTradingBalance()
    }

    @Tool(description = "獲取指數5分鐘即時資料")
    fun getIndices5MinRealtime(): List<Map<String, Any>> {
        return twStockClient.getIndices5MinRealtime()
    }

    // 交易資訊
    @Tool(description = "獲取所有股票每日交易資訊")
    fun getStockDailyInfo(): List<Map<String, Any>> {
        return twStockClient.getStockDailyInfo()
    }

    @Tool(description = "獲取所有股票每日平均價格")
    fun getStockDailyAverage(): List<Map<String, Any>> {
        return twStockClient.getStockDailyAverage()
    }

    @Tool(description = "獲取前48檔熱門股票資訊")
    fun getTop48StocksInfo(): List<Map<String, Any>> {
        return twStockClient.getTop48StocksInfo()
    }

    @Tool(description = "獲取ETF排行榜")
    fun getETFRanking(): List<Map<String, Any>> {
        return twStockClient.getETFRanking()
    }

    @Tool(description = "獲取融資買進餘額")
    fun getMarginBuyingBalance(): List<Map<String, Any>> {
        return twStockClient.getMarginBuyingBalance()
    }

    @Tool(description = "獲取融券賣出餘額")
    fun getShortSellingBalance(): List<Map<String, Any>> {
        return twStockClient.getShortSellingBalance()
    }

    @Tool(description = "獲取估價指標")
    fun getValuationIndicators(): List<Map<String, Any>> {
        return twStockClient.getValuationIndicators()
    }

//    @Prompt(description = "Make a greeting")
//    fun makeGreeting(@PromptArg(description = "Who you want to be greeted") who: String): PromptMessage {
//        return PromptMessage.withUserRole(
//            TextContent(
//                """
//            The assistants goal is to greet the user $who.
//        """.trimIndent()
//            )
//        )
//    }
}
