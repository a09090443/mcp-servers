package tw.zipe.mcp.twse

import jakarta.inject.Singleton
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@Singleton
@RegisterRestClient(baseUri = "https://openapi.twse.com.tw/v1")
interface TWStockClient {

    // 公司基礎與財務
    @GET
    @Path("/opendata/t187ap03_L")
    fun getCompanyBasicInfo(): List<Map<String, Any>>

    @GET
    @Path("/opendata/t187ap04_L")
    fun getCompanyEPS(): List<Map<String, Any>>

    @GET
    @Path("/opendata/t187ap05_L")
    fun getCompanyMonthlyRevenue(): List<Map<String, Any>>

    @GET
    @Path("/opendata/t187ap45_L")
    fun getCompanyDividendInfo(): List<Map<String, Any>>

    // 董監與股權資訊細分
    @GET
    @Path("/opendata/t187ap06_L_{category}")
    fun getCompanyShareholdingInfo(@PathParam("category") category: String): List<Map<String, Any>>

    // t187ap07_L 系列（資產負債表細分）
    @GET
    @Path("/opendata/t187ap07_L_{category}")
    fun getCompanyBalanceSheet(@PathParam("category") category: String): List<Map<String, Any>>

    // ESG 相關
    @GET
    @Path("/opendata/t187ap46_L_8")
    fun getCompanyESGSustainabilityReport(): List<Map<String, Any>>

    @GET
    @Path("/opendata/t187ap46_L_9")
    fun getCompanyESGFunctionalCommittee(): List<Map<String, Any>>

    @GET
    @Path("/opendata/t187ap46_L_13")
    fun getCompanyESGSupplyChain(): List<Map<String, Any>>

    @GET
    @Path("/opendata/t187ap46_L_16")
    fun getCompanyESGOtherInfo(): List<Map<String, Any>>

    @GET
    @Path("/opendata/t187ap46_L_19")
    fun getCompanyESGRiskManagement(): List<Map<String, Any>>

    // 上市權證基本資料彙總表
    @GET
    @Path("/opendata/t187ap37_L")
    fun getCompanyAnnouncements(): List<Map<String, Any>>

    // 上市認購(售)權證每日成交資料檔
    @GET
    @Path("/opendata/t187ap42_L")
    fun getCompanyFinancialData(): List<Map<String, Any>>

    @GET
    @Path("/opendata/t187ap43_L")
    fun getCompanyBusinessData(): List<Map<String, Any>>

    // 新聞與事件
    @GET
    @Path("/news/newsList")
    fun getNewsList(): List<Map<String, Any>>

    @GET
    @Path("/news/eventList")
    fun getEventList(): List<Map<String, Any>>

    // 市場資訊
    @GET
    @Path("/fund/MI_QFIIS_cat")
    fun getQFIICategories(): List<Map<String, Any>>

    @GET
    @Path("/fund/MI_QFIIS_sort_20")
    fun getQFIITop20Holdings(): List<Map<String, Any>>

    @GET
    @Path("/exchangeReport/MI_INDEX")
    fun getMarketIndices(): List<Map<String, Any>>

    @GET
    @Path("/indicesReport/MI_5MINS_HIST")
    fun getIndices5MinHistory(): List<Map<String, Any>>

    @GET
    @Path("/exchangeReport/MI_MARGN")
    fun getMarginTradingBalance(): List<Map<String, Any>>

    @GET
    @Path("/exchangeReport/MI_5MINS")
    fun getIndices5MinRealtime(): List<Map<String, Any>>

    // 交易資訊
    @GET
    @Path("/exchangeReport/STOCK_DAY_ALL")
    fun getStockDailyInfo(): List<Map<String, Any>>

    @GET
    @Path("/exchangeReport/STOCK_DAY_AVG_ALL")
    fun getStockDailyAverage(): List<Map<String, Any>>

    @GET
    @Path("/exchangeReport/TWT48U_ALL")
    fun getTop48StocksInfo(): List<Map<String, Any>>

    @GET
    @Path("/ETFReport/ETFRank")
    fun getETFRanking(): List<Map<String, Any>>

    @GET
    @Path("/exchangeReport/FMSRFK_ALL")
    fun getMarginBuyingBalance(): List<Map<String, Any>>

    @GET
    @Path("/exchangeReport/FMNPTK_ALL")
    fun getShortSellingBalance(): List<Map<String, Any>>

    @GET
    @Path("/exchangeReport/BWIBBU_ALL")
    fun getValuationIndicators(): List<Map<String, Any>>
}
