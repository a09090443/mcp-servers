package tw.zipe.mcp.twse.enumerate

enum class IndustryCategory(val description: String, val keyWord: List<String>) {
    BD("證券期貨業", listOf("證券股份", "期貨股份")),
    CI("一般業", listOf()),
    FH("金控業", listOf("金融控股")),
    INS("保險業", listOf("保險股份")),
    MIM("異業", listOf()),
    BASI("金融業", listOf());

    companion object {
        fun findByKeyWord(input: String): IndustryCategory {
            return entries.find { category ->
                category.keyWord.any { keyword -> input.contains(keyword) }
            } ?: CI
        }
    }
}
