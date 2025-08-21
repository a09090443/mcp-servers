# TWSE/MOPS API 清單與用途說明

> 說明：本清單彙整此前討論內容，依主題分類，便於快速查找與整合。若需逐條比對官方 Swagger UI 的摘要、欄位（schema）與更新頻率，請以官方文件為準。

---

## 一、公司（Company）

### 基礎與財務
| API 路徑 | 用途說明 |
|---|---|
| /opendata/t187ap03_L | 上市公司基本資料（代號、名稱、產業類別等） |
| /opendata/t187ap04_L | 上市公司每股盈餘（EPS） |
| /opendata/t187ap05_L | 上市公司每月營收 |
| /opendata/t187ap45_L | 上市公司股利分派情形（歷史派息與政策） |

### 董監與股權、資產負債表
| API 路徑 | 用途說明 |
|---|---|
| /opendata/t187ap06_L | 股東持股及變動資訊彙總 |

### t187ap07_L 系列（資產負債表細分）
| API 路徑 | 口徑/類型 | 用途說明 |
|---|---|---|
| /opendata/t187ap07_L_bd | 產業別（銀行） | 銀行業口徑資產負債表（含金融特有科目） |
| /opendata/t187ap07_L_ci | 產業別（製造/水泥，命名推定） | 針對特定製造/水泥業的資產負債表 |
| /opendata/t187ap07_L_fh | 產業別（金控） | 金融控股口徑的資產負債表（母/子公司結構） |
| /opendata/t187ap07_L_ins | 產業別（保險） | 保險業口徑資產負債表（保險負債、準備金等） |
| /opendata/t187ap07_L_mim | 彙總 | 市場/行業彙總資產負債表（aggregated） |
| /opendata/t187ap07_L_basi | 精簡欄位 | 基本欄位版資產負債表（通用/精簡） |

### ESG 與其他
| API 路徑 | 用途說明 |
|---|---|
| /opendata/t187ap46_L_8 | ESG 揭露－企業永續報告指標 |
| /opendata/t187ap46_L_9 | ESG 揭露－功能性委員會 |
| /opendata/t187ap46_L_13 | ESG 揭露－供應鏈管理 |
| /opendata/t187ap46_L_16 | ESG 揭露－其他揭露項 |
| /opendata/t187ap46_L_19 | ESG 揭露－風險管理政策 |
| /opendata/t187ap37_L | 公司公告/財務揭露類（待官方描述核對） |
| /opendata/t187ap42_L | 公司財務數據類（待官方描述核對） |
| /opendata/t187ap43_L | 業務/財務公開資料類（待官方描述核對） |

---

## 二、新聞與事件（News/Events）
| API 路徑 | 用途說明 |
|---|---|
| /news/newsList | 證券市場新聞列表 |
| /news/eventList | 市場重大事件列表 |

---

## 三、市場資訊（Market）
| API 路徑 | 用途說明 |
|---|---|
| /fund/MI_QFIIS_cat | QFII（合格境外機構投資人）類別 |
| /fund/MI_QFIIS_sort_20 | QFII 持股前20名排行 |
| /exchangeReport/MI_INDEX | 大盤指數資訊 |
| /indicesReport/MI_5MINS_HIST | 大盤指數每5分鐘歷史資料 |
| /exchangeReport/MI_MARGN | 融資融券餘額 |
| /exchangeReport/MI_5MINS | 大盤指數每5分鐘即時資料 |

---

## 四、交易資訊（Trading）
| API 路徑 | 用途說明 |
|---|---|
| /exchangeReport/STOCK_DAY_ALL | 所有股票每日交易彙總 |
| /exchangeReport/STOCK_DAY_AVG_ALL | 所有股票每日平均交易資訊 |
| /exchangeReport/TWT48U_ALL | 48檔權值股交易資訊 |
| /ETFReport/ETFRank | ETF 排名資訊 |
| /exchangeReport/FMSRFK_ALL | 融資買進餘額 |
| /exchangeReport/FMNPTK_ALL | 融券賣出餘額 |
| /exchangeReport/BWIBBU_ALL | 估值指標彙整（如殖利率/本益比/股價淨值比等） |

---

## 備註與建議
- 實際欄位名稱、參數、更新頻率與授權條款，請以官方文件（例如 Swagger UI/資料集頁面）為準。
- t187ap07_L_* 為資產負債表主題的產業別/彙總/精簡等口徑細分，建議下載近期 CSV 檔對照欄位，以確認產業特有科目與合併/個別口徑差異。
- 若需要為各 API 補上「欄位清單、期間格式（年度/季別/日期）、示例回應」等資訊，請說明欲覆蓋的 API 範圍與重點欄位。
