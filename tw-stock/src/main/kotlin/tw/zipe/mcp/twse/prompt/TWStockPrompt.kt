package tw.zipe.mcp.twse.prompt

import io.quarkiverse.mcp.server.Prompt
import io.quarkiverse.mcp.server.PromptArg
import io.quarkiverse.mcp.server.PromptMessage
import io.quarkiverse.mcp.server.TextContent

class TWStockPrompt {

    @Prompt(description = "股息投資策略")
    fun dividendInvestmentStrategyPrompt(
        @PromptArg(
            name = "strategy_type",
            description = "high_yield:高收益／高殖利率, growth:成長／增長, timing:時機／入場時點, sector:行業／產業",
            defaultValue = "high_yield"
        ) strategyType: String,
        @PromptArg(
            name = "time_horizon",
            description = "quarterly:季度, annual:年度",
            defaultValue = "quarterly"
        ) timeHorizon: String
    ): PromptMessage = PromptMessage.withUserRole(
        TextContent(
            """
                您是一位台灣股市股息投資專家，專精於利用 TWSE OpenAPI 端點制定全面的股息投資策略。請根據以下資訊，提供具體且可執行的投資建議，並將分析過程及結果輸出至 `analysis.md` 中。

                **可使用的 TWSE OpenAPI 端點（用於股息分析）：**

                **股息時程與規劃：**
                * `getTop48StocksInfo(code)`：查詢股票代碼的除權息日期與股息金額。
                * `getCompanyDividendInfo(code)`：查詢公司歷史股利發放數據。
                * `getStockValuationRatios(code)`：查詢本益比、股息殖利率、股價淨值比等估值指標。

                **基本面分析：**
                * `getCompanyBasicInfo(code)`：查詢公司基本資訊與產業分類。
                * `getCompanyMonthlyRevenue(code)`：查詢公司每月營收趨勢，用於評估股息可持續性。
                * `getCompanyIncomeStatement(code)`：查詢公司損益表數據，用於分析配發率。
                * `getCompanyBalanceSheet(code)`：查詢公司資產負債表數據，用於評估財務實力。

                **市場驗證：**
                * `getTop20InvestorHoldingsSummary()`：查詢外國投資者持有較多的股票，了解外資對股息股的偏好。
                * `getETFRanking()`：查詢定期定額 ETF 投資排行榜，了解市場熱門的股息導向投資趨勢。
                * `getStockDailyInfo(code)`：查詢股票每日交易數據，分析除權息日期前後的價格穩定性。

                **投資策略類型（strategy_type）：**

                請根據指定的策略類型，選擇性地使用上述 API 端點，並提供相應的分析和建議。

                * **高殖利率策略 (high_yield)：** 專注於具有吸引力股息殖利率的股票。
                    * 步驟：
                        1. 使用 `getStockValuationRatios()` 篩選高股息殖利率股票（可設定最低殖利率門檻，例如 >4%）。
                        2. 使用 `getCompanyIncomeStatement()` 和 `getCompanyMonthlyRevenue()` 驗證股息的可持續性。
                        3. 檢查 `getTop48StocksInfo()` 以了解即將到來的股息發放。
                        4. 透過 `getTop20InvestorHoldingsSummary()` 評估外資興趣。

                * **股息增長策略 (growth)：** 鎖定股息持續增長的公司。
                    * 步驟：
                        1. 分析 `getCompanyDividendInfo()` 的歷史股息增長模式。
                        2. 與 `getCompanyMonthlyRevenue()` 交叉引用，了解業務增長情況。
                        3. 使用 `getCompanyBalanceSheet()` 確保財務實力。
                        4. 監控 `getTop20InvestorHoldingsSummary()` 的外資趨勢，作為品質驗證。

                * **除權息時機策略 (timing)：** 優化除權息日期前後的進出場時機。
                    * 步驟：
                        1. 使用 `getTop48StocksInfo()` 進行精確計時。
                        2. 分析 `getStockDailyInfo()` 的歷史價格模式。
                        3. 考慮使用 `getStockValuationRatios()` 進行公允價值評估。
                        4. 將即將到來的股息公告納入考量。

                * **產業股息策略 (sector)：** 專注於特定支付股息的產業。
                    * 步驟：
                        1. 透過 `getCompanyBasicInfo()` 按產業對公司進行分組。
                        2. 比較不同產業的股息殖利率與可持續性。
                        3. 分析外國投資者的產業偏好。
                        4. 在穩定的股息支付產業中進行多元化投資。

                **時間範圍分析（time_horizon）：**

                * **季度焦點 (quarterly)：** 關注未來 3 個月的股息相關資訊。
                    * 分析重點：
                        1. 未來 3 個月的除權息時程。
                        2. 季度盈餘對股息可持續性的影響。
                        3. 除權息日期前後的短期價格波動。

                * **年度規劃 (annual)：** 關注全年度的股息相關資訊。
                    * 分析重點：
                        1. 全年度股息日曆規劃。
                        2. 年度股息增長趨勢分析。
                        3. 長期產業輪動策略。

                **分析框架（通用）：**

                1. **股息篩選：** 根據策略類型，篩選目標股息殖利率或增長率，驗證即將到來的除權息日期與金額，檢查歷史股息的一致性。
                2. **基本面驗證：** 評估盈餘覆蓋率與配發率，檢視營收穩定性與增長趨勢，分析資產負債表實力與現金流。
                3. **市場定位：** 與外國投資偏好進行比較，監控熱門的股息投資趨勢，評估市場時機與估值水平。

                **輸出格式：**

                **📊 股息投資建議：**

                **熱門股息股：**
                1. **公司名稱 (股票代碼)：** 殖利率 X%，除權息日：YYYY-MM-DD，簡要投資理由 (基於API數據的分析結果)
                2. **公司名稱 (股票代碼)：** 殖利率 X%，除權息日：YYYY-MM-DD，簡要投資理由 (基於API數據的分析結果)
                3. [依據篩選結果增加]

                **📅 股息日曆 (未來 3 個月):**
                * 第 1 週 (YYYY-MM-DD ~ YYYY-MM-DD)：公司名稱 (股票代碼)，預期殖利率
                * 第 2 週 (YYYY-MM-DD ~ YYYY-MM-DD)：公司名稱 (股票代碼)，預期殖利率
                * [依規劃期間繼續]

                **🎯 策略洞察：**
                * **風險評估：**  詳細說明股息可持續性的風險因素 (例如：產業景氣下滑、公司營收衰退等)。
                * **進場時機：**  根據歷史價格波動與市場情緒，建議最佳購買窗口。
                * **投資組合建構：**  根據風險承受度，提供多元化投資建議 (例如：配置不同產業的股息股)。
                * **市場狀況：**  分析當前總體經濟環境與股市氛圍對股息投資的影響。

                **💡 進階策略：** (選擇性提供)
                * 股息捕捉機會：分析除權息前後的股價行為，尋找短期獲利機會。
                * 基於股息週期的產業輪動：根據不同產業的股息發放時間，進行產業配置。
                * 稅務優化考量：提醒股息收入的相關稅務規定。
                * 外資流動影響：分析外資買賣超對股價的影響。

                **當前策略請求：**

                * 策略類型（strategy_type）：${'$'}{strategyType ?: 'high_yield'}  // 如果 strategyType 為空，則預設為 'high_yield'
                * 時間範圍（time_horizon）：${'$'}{timeHorizon ?: 'quarterly'}  // 如果 timeHorizon 為空，則預設為 'quarterly'

                請根據以上資訊，利用 TWSE OpenAPI 端點，產出結構化的股息投資建議。請在回答中明確引用API的數據分析結果，並將分析過程及結果以 Markdown 格式輸出至 `analysis.md` 檔案中。

                **progress.md 筆記格式** (請在執行過程中更新)
                
                ## 任務進度
                - [x] 理解提示詞與目標
                - [ ] 根據 strategyType 與 timeHorizon 選擇適當的 API 端點
                - [ ] 分析 API 回傳的數據
                - [ ] 產生結構化的股息投資建議
                - [ ] 更新 progress.md
                
                **analysis.md 檔案內容範例：**

                ```markdown
                ## 股息投資分析報告
                
                **策略類型：** 高殖利率 (High Yield)
                **時間範圍：** 季度焦點 (Quarterly)
                
                ### 1. 股息篩選
                
                *   **篩選標準：** 股息殖利率 > 4%
                *   **API 使用：** `getStockValuationRatios()`
                *   **篩選結果：**
                    *   聯電 (2303)：殖利率 6.88%
                    *   華碩 (2357)：殖利率 5.14%
                    *   ...
                
                ### 2. 基本面驗證
                
                *   **驗證指標：** 盈餘覆蓋率、營收穩定性
                *   **API 使用：** `getCompanyIncomeStatement()`, `getCompanyMonthlyRevenue()`
                *   **分析結果：**
                    *   聯電 (2303)：盈餘覆蓋率良好，但近幾個月營收略有下滑...
                    *   華碩 (2357)：盈餘覆蓋率穩定，營收成長...
                    *   ...
                
                ### 3. 市場定位
                
                *   **分析指標：** 外資持股比例、ETF 投資趨勢
                *   **API 使用：** `getTop20InvestorHoldingsSummary()`, `getETFRanking()`
                *   **分析結果：**
                    *   聯電 (2303)：外資持股比例穩定...
                    *   華碩 (2357)：外資近期增持...
                    *   ...
                
                ### 4. 結論
                
                *   **投資建議：**
                    *   聯電 (2303)：...
                    *   華碩 (2357)：...
                    *   ...
    """.trimIndent()
        )
    )

    @Prompt(description = "外資投資分析")
    fun foreignInvestmentAnalysisPrompt(
        @PromptArg(
            name = "analysis_type",
            description = "overview:概覽, industry:產業, top_holdings:主要持股, stock:個股",
            defaultValue = "overview"
        ) analysisType: String,
        @PromptArg(name = "industry", description = "目標產業 (可選)", defaultValue = "") industry: String,
        @PromptArg(name = "stock_symbol", description = "目標股票代號 (可選)", defaultValue = "")
        stockSymbol: String
    ): PromptMessage = PromptMessage.withUserRole(
        TextContent(
            """
        台灣股市外資投資分析：

        您是一位台灣股市外資投資專家。請根據請求的分析類型，使用以下 TWSE OpenAPI 端點提供全面的外資投資洞察：

        ### 可用 API 及其用途：

        **外資投資概覽：**
        - `get_foreign_investment_by_industry()`：按產業類別劃分的外資持股比例
        - `getTop20InvestorHoldingsSummary()`：外資持股前 20 大公司
        - `getCompanyBasicInfo(code)`：公司基本資訊以供參考

        **進階分析：**
        - `get_market_index_info(category, count, format)`：市場背景與產業表現
          - 使用 `category="sector", format="summary"` 以了解產業表現背景
          - 使用 `category="major", count=5, format="simple"` 以了解整體市場情緒
        - `getCompanyDividendInfo(code)`：外資偏好股票的股息資訊
        - `getStockValuationRatios(code)`：本益比、股息殖利率、股價淨值比
        - `getCompanyMonthlyRevenue(code)`：營收表現數據

        ### 分析類型：

        **1. 產業分析 (analysis_type="industry"):**
        使用 `get_foreign_investment_by_industry()` 進行分析：
        - 哪些產業最吸引外資
        - 按產業劃分的外資持股百分比
        - 每個產業中擁有外資的公司數量
        - 特定產業的投資模式

        **2. 主要持股分析 (analysis_type="top_holdings"):**
        使用 `getTop20InvestorHoldingsSummary()` 和 `getCompanyBasicInfo()` 進行分析：
        - 外資持股前 20 大公司
        - 可用投資額度與當前持股
        - 投資上限與限制
        - 外資偏好股票的公司簡介

        **3. 特定股票分析 (analysis_type="stock", 提供 stock_symbol):**
        結合多個 API 進行 stock_symbol 分析：
        - 外資持股部位與限制
        - 公司基本面與股息政策
        - 估值指標與外資趨勢比較

        ### 範例輸出格式：

        **外資產業分析：**
        - **科技業**：平均外資持股 45.2%，120 家公司，由半導體和電子業驅動
        - **金融服務業**：平均外資持股 32.1%，集中在主要銀行
        - **醫療保健業**：平均外資持股 28.7%，對生物技術的興趣日益濃厚

        **主要外資持股洞察：**
        1. **台積電 (2330)**：外資持股 78%，接近投資上限，基本面強勁支撐
        2. **鴻海 (2317)**：外資持股 65%，尚有大量可投資額度，電子代工服務領導者
        3. **聯發科 (2454)**：外資持股 72%，專注於晶片設計，估值具吸引力

        **投資建議：**
        根據外資投資模式，考慮具有以下特點的股票：
        - 外資興趣濃厚但尚有可投資額度
        - 穩健的基本面支撐外資信心
        - 呈現外資配置增加趨勢的產業

        ### 當前分析請求：
        分析類型：${analysisType}
        {f"目標產業：${industry}" if industry else ""}
        {f"目標股票：${stockSymbol}" if stock_symbol else ""}

        請使用上述適當的 API 提供全面的外資投資分析。
    """.trimIndent()
        )
    )

    @Prompt(description = "投資篩選")
    fun investmentScreeningPrompt(
        @PromptArg(
            name = "screening_criteria",
            description = "value:價值, growth:成長, dividend:股息, momentum:動能, comprehensive:綜合",
            defaultValue = "comprehensive"
        ) screeningCriteria: String,
        @PromptArg(
            name = "risk_level",
            description = "conservative:保守, moderate:穩健, aggressive:積極",
            defaultValue = "moderate"
        ) riskLevel: String
    ): PromptMessage =
        PromptMessage.withUserRole(
            TextContent(
                """
        台灣股市投資篩選：

        您是一位台灣股市投資篩選專家。請使用以下 TWSE OpenAPI 端點，根據多重標準篩選並推薦投資機會：

        ### 可用於投資篩選的 API：

        **估值與表現：**
        - `getStockValuationRatios(code)`：本益比、股息殖利率、股價淨值比
        - `getStockDailyInfo(code)`：價格表現與成交量
        - `get_stock_monthly_trading(code)`：月度表現趨勢
        - `getCompanyMonthlyRevenue(code)`：營收增長模式

        **品質指標：**
        - `getCompanyBasicInfo(code)`：公司基本資訊與產業
        - `getCompanyIncomeStatement(code)`：獲利能力與增長指標
        - `getCompanyBalanceSheet(code)`：財務實力與穩定性
        - `getCompanyDividendInfo(code)`：股息歷史與一致性

        **市場驗證：**
        - `get_market_index_info(category, count, format)`：產業表現與市場背景
          - 使用 `category="sector", format="summary"` 識別表現優異的產業
          - 使用 `category="esg", count=10` 作為 ESG 投資領域
          - 使用 `category="dividend", format="simple"` 進行以收益為重點的篩選
        - `getTop20InvestorHoldingsSummary()`：外國投資者偏好 (品質信號)
        - `getETFRanking()`：熱門的散戶投資選擇
        - `get_margin_trading_info()`：法人與散戶興趣
        - `get_foreign_investment_by_industry()`：產業配置趨勢

        **風險評估：**
        - `get_company_major_news(code)`：近期公司發展
        - `getTop48StocksInfo(code)`：即將進行的公司活動
        - `get_warrant_daily_trading(code)`：投機活動水平

        ### 篩選標準：

        **1. 價值投資 (screening_criteria="value"):**
        - 低本益比 (<15) 且盈餘穩定
        - 高股息殖利率 (>3%) 且可持續配發
        - 低股價淨值比 (<1.5) 且資產負債表強健
        - 外資驗證以確認品質

        **2. 成長投資 (screening_criteria="growth"):**
        - 持續的營收增長 (年增 >10%)
        - 損益表顯示利潤率擴大
        - 外資持股增加 (成長認可)
        - 在 ETF 定期定額投資排名中受歡迎

        **3. 股息焦點 (screening_criteria="dividend"):**
        - 高股息殖利率且有持續的支付歷史
        - 來自營運的強勁現金流
        - 穩定或增長的股息趨勢
        - 外國投資者對股息股的偏好

        **4. 動能與趨勢 (screening_criteria="momentum"):**
        - 近期價格表現強勁並有成交量確認
        - 正面的新聞流與公司發展
        - 外資興趣增加
        - 高於平均的交易活動

        **5. 綜合分析 (screening_criteria="comprehensive"):**
        結合所有因素以提供平衡的建議：
        - 價值指標在合理範圍內
        - 具有品質指標的增長前景
        - 股息可持續性與殖利率吸引力
        - 透過外資/法人興趣進行市場驗證

        ### 風險等級調整：

        **保守型 ({risk_level}="conservative"):**
        - 具有穩定商業模式的大型股公司
        - 強健的資產負債表與持續的股息
        - 高外資持股 (>30%) 作為品質指標
        - 防禦性產業中的市場領導者

        **穩健型 ({risk_level}="moderate"):**
        - 大型股與中型股的混合機會
        - 具有合理估值的成長
        - 中等外資持股 (15-30%)
        - 平衡的風險回報特徵

        **積極型 ({risk_level}="aggressive"):**
        - 中小型成長股機會
        - 較高的增長率與可接受的估值
        - 新興趨勢與市場主題
        - 透過權證活動進行部分投機

        ### 篩選流程：

        **步驟 1：界定範圍**
        - 使用 `getTop20InvestorHoldingsSummary()` 和 `getETFRanking()` 界定優質股範圍
        - 按市值與流動性要求進行篩選
        - 考慮產業多元化需求

        **步驟 2：量化篩選**
        - 應用估值、增長與股息標準
        - 篩選財務實力指標
        - 分析交易模式與成交量趨勢

        **步驟 3：質化驗證**
        - 檢視近期新聞與公司發展
        - 評估外國投資者興趣趨勢
        - 評估即將進行的公司活動影響

        **步驟 4：風險評估**
        - 檢查融資融券水平以評估投機風險
        - 檢視權證活動作為情緒指標
        - 評估產業與市場集中度風險

        ### 輸出格式：

        **🎯 投資建議：**

        **首選股票：**
        1. **公司 (代碼)**：理由、關鍵指標、風險等級
        2. **公司 (代碼)**：理由、關鍵指標、風險等級
        3. **公司 (代碼)**：理由、關鍵指標、風險等級

        **📊 按標準篩選結果：**
        - **價值股**：最佳本益比、股價淨值比、股息殖利率組合
        - **成長股**：營收增長、利潤率擴張、市場擴張
        - **股息股**：殖利率、可持續性、增長潛力
        - **外資最愛**：高外資持股且基本面強勁

        **⚖️ 風險回報分析：**
        - 按風險等級劃分的預期回報概況
        - 多元化建議
        - 產業配置建議
        - 部位規模指引

        **📈 市場背景：**
        - 當前市場環境評估
        - 產業輪動趨勢與機會
        - 外資流動影響
        - 進場時機考量

        **🔍 進一步研究建議：**
        - 需要更深入基本面分析的公司
        - 值得關注的新興趨勢
        - 潛在催化劑與風險因素
        - 投資組合建構考量

        ### 當前篩選請求：
        篩選標準：${screeningCriteria}
        風險等級：${riskLevel}

        請使用上述適當的 API 提供全面的投資篩選。
    """.trimIndent()
            )
        )


    @Prompt(description = "市場熱點監控")
    fun marketHotspotMonitoringPrompt(
        @PromptArg(
            name = "monitoring_scope",
            description = "news:新聞, trading:交易, comprehensive:綜合",
            defaultValue = "comprehensive"
        )
        monitoringScope: String,
        @PromptArg(name = "date_filter", description = "today:今日, week:本週, month:本月", defaultValue = "today")
        dateFilter: String
    ): PromptMessage =
        PromptMessage.withUserRole(
            """
        台灣股市市場熱點監控：

        您是一位台灣股市監控專家。請使用以下 TWSE OpenAPI 端點來識別市場熱點、熱門股票和重要的市場發展：

        ### 可用於熱點偵測的 API：

        **新聞與公告：**
        - `get_company_major_news(code)`：公司重大公告 (可按公司篩選或取得全部)
        - `get_twse_news(start_date, end_date)`：最新的證交所官方新聞與公告 (可選日期篩選)
        - `get_twse_events(top=10)`：證交所活動

        **市場活動指標：**
        - `get_real_time_trading_stats()`：即時市場統計數據
        - `get_market_index_info(category, count, format)`：市場指數分析，具備彈性篩選功能
          - 使用 `category="major", format="summary"` 快速瀏覽市場概況
          - 使用 `category="sector", format="simple"` 識別熱門產業
          - 使用 `category="thematic", count=10` 了解趨勢主題 (AI、5G、ESG 等)
        - `getStockDailyInfo(code)`：每日成交量與價格波動
        - `getETFRanking()`：熱門 ETF 投資趨勢
        - `getTop20InvestorHoldingsSummary()`：外資流動指標

        **公司活動：**
        - `getTop48StocksInfo(code)`：即將到來的除權息日期
        - `get_warrant_daily_trading(code)`：權證交易活動 (投機指標)

        ### 監控範圍：

        **1. 重大新聞監控 (monitoring_scope="news"):**
        專注於 `get_company_major_news()` 和 `get_twse_news(start_date, end_date)` 以識別：
        - 今日有重大公告的公司
        - 監管變更或政策更新
        - 影響市場的新聞與公司活動
        - 特定產業的發展

        **2. 交易活動熱點 (monitoring_scope="trading"):**
        使用交易相關 API 尋找：
        - 成交量或價格波動異常的股票
        - 在定期定額投資計畫中越來越受歡迎的 ETF
        - 表明市場投機的權證活動
        - 外資流動變化

        **3. 全面市場掃描 (monitoring_scope="comprehensive"):**
        結合所有 API 以獲得完整的市場概況：
        - 影響多個產業的重大新聞
        - 成交量與外資模式
        - 即將進行的公司活動及其潛在影響
        - 整體市場情緒指標

        ### 分析框架：

        **步驟 1：新聞影響評估**
        - 掃描重大公告以尋找影響市場的潛力
        - 識別近期有多個公告的公司
        - 評估監管或政策對產業的影響

        **步驟 2：交易模式分析**
        - 將當前成交量與歷史平均水平進行比較
        - 識別外資流動變化
        - 監控 ETF 與權證活動以了解市場情緒

        **步驟 3：前瞻性指標**
        - 即將到來的除權息日期對股價的影響
        - 可能推動未來交易的公司活動
        - 可能影響市場結構的證交所活動

        ### 輸出格式：

        **市場熱點摘要：**
        🔥 **今日熱門焦點：**
        1. **公司/產業**：受關注原因、相關新聞、交易影響
        2. **市場主題**：描述、受影響股票、趨勢分析

        📈 **交易活動警示：**
        - 有新聞支撐的高成交量股票
        - 外資流動變化
        - 熱門投資趨勢 (ETF 排名)

        📅 **即將發生的事件：**
        - 未來 5 個交易日內除權息日期
        - 重要的證交所公告或活動
        - 值得關注的公司活動

        🎯 **投資意涵：**
        - 短期交易機會
        - 中期趨勢轉變
        - 需要監控的風險因素

        ### 當前監控請求：
        監控範圍：${monitoringScope}
        日期篩選：${dateFilter}

        請使用上述適當的 API 提供全面的市場熱點分析。
    """.trimIndent()
        )


    @Prompt(description = "個股趨勢分析")
    fun getStockTrendAnalysisPrompt(
        @PromptArg(name = "stock_symbol", description = "股票代號")
        stockSymbol: String,
        @PromptArg(
            name = "period",
            description = "分析期間 (short_term:短期, mid_term:中期, long_term:長期)",
            defaultValue = "short_term"
        )
        period: String
    ): PromptMessage = PromptMessage.withUserRole(
        TextContent(
            """
        使用 TWSE OpenAPI 端點進行台灣股市趨勢分析的提示：

        您是一位台灣股市趨勢分析專家。請根據使用者輸入的股票代號和分析期間 (短期/中期/長期)，自動選擇並利用以下全面的 TWSE MCP 工具，從多個角度 (技術、基本面、籌碼、市場情緒和新聞) 進行分析。請以條列式清晰地呈現您的推理和結論：

        ### 按分析時間範圍劃分的可用 MCP 工具：

        - **短期分析 (1-30 天)：**
          - **技術面**：`getStockDailyInfo(code)` - 每日價格、成交量和交易統計
          - **技術面**：`get_real_time_trading_stats()` - 即時市場統計 (5 秒更新)
          - **籌碼面**：`get_margin_trading_info()` - 融資融券數據
          - **籌碼面**：`get_foreign_investment_by_industry()` - 按產業劃分的外資流動
          - **市場情緒**：`get_warrant_daily_trading(code)` - 權證交易活動 (槓桿指標)
          - **新聞面**：`get_company_major_news(code)` - 近期重大公告

        - **中期分析 (1-12 個月)：**
          - **技術面**：`get_stock_monthly_average(code)` - 每月平均價格和成交量
          - **技術面**：`get_stock_monthly_trading(code)` - 每月交易統計
          - **基本面**：`getCompanyMonthlyRevenue(code)` - 每月營收趨勢
          - **基本面**：`getCompanyIncomeStatement(code)` - 季度損益表
          - **基本面**：`getCompanyBalanceSheet(code)` - 資產負債表數據
          - **籌碼面**：`getTop20InvestorHoldingsSummary()` - 主要股票中的外資集中度
          - **事件面**：`getTop48StocksInfo(code)` - 即將到來的除權息日期

        - **長期分析 (1年以上)：**
          - **技術面**：`get_stock_yearly_trading(code)` - 年度交易模式
          - **估值面**：`getStockValuationRatios(code)` - 本益比、股息殖利率、股價淨值比
          - **基本面**：`getCompanyDividendInfo(code)` - 股息歷史與政策
          - **ESG**：`get_company_governance_info(code)` - 公司治理品質
          - **市場背景**：`get_market_historical_index()` - 歷史市場表現

        ### 輸入範例：
        請分析 ${stockSymbol} 的 ${period} 趨勢。

        ### 輸出範例：

        **短期分析：**
        1. **技術面**：使用 `getStockDailyInfo(${stockSymbol})`，股價已帶量站上 20 日均線。`get_real_time_trading_stats()` 確認了強勁的盤中動能。
        2. **籌碼面**：`get_margin_trading_info()` 顯示融資餘額減少、融券回補增加，顯示看漲情緒。`get_foreign_investment_by_industry()` 顯示該產業有外資淨買入。
        3. **市場情緒**：`get_warrant_daily_trading(${stockSymbol})` 顯示認購權證活動增加，暗示看漲的槓桿部位。
        4. **新聞面**：`get_company_major_news(${stockSymbol})` 顯示有正面公告支撐上漲趨勢。

        **中期分析：**
        1. **技術面**：`get_stock_monthly_average(${stockSymbol})` 和 `get_stock_monthly_trading(${stockSymbol})` 顯示持續的上升趨勢和健康的成交量模式。
        2. **基本面**：`getCompanyMonthlyRevenue(${stockSymbol})` 顯示持續增長。`getCompanyIncomeStatement(${stockSymbol})` 和 `getCompanyBalanceSheet(${stockSymbol})` 確認獲利能力和財務狀況改善。
        3. **籌碼面**：`getTop20InvestorHoldingsSummary()` 指出該股票正獲得外資法人的興趣。
        4. **事件面**：`getTop48StocksInfo(${stockSymbol})` 顯示即將到來的除權息日期可能支撐股價。

        **長期分析：**
        1. **技術面**：`get_stock_yearly_trading(${stockSymbol})` 顯示在強勁基本面支撐下的多年上升趨勢。
        2. **估值面**：`getStockValuationRatios(${stockSymbol})` 指出具吸引力的本益比、可持續的股息殖利率和合理的股價淨值比。
        3. **基本面**：`getCompanyDividendInfo(${stockSymbol})` 顯示持續的股息增長政策。
        4. **ESG**：`get_company_governance_info(${stockSymbol})` 指出強健的公司治理支持長期價值。
        5. **市場背景**：`get_market_historical_index()` 顯示整體市場環境仍然有利。

        ### 結論：
        根據以上分析，${stockSymbol} 的 ${period} 趨勢為 **看漲/看跌**。
    """.trimIndent()
        )
    )

}
