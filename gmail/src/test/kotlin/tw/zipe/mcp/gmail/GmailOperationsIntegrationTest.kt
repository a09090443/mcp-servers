package tw.zipe.mcp.gmail

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class GmailOperationsIntegrationTest {

    private lateinit var gmailOperations: GmailOperations
    private val gson = Gson()
    private val testEmailAddress = "test@example.com" // 替換為你實際的測試郵件地址
    private var testMessageId: String? = null

    @BeforeEach
    fun setup() {
        // 確保環境變數已設定
        val credentialsPath = System.getenv("GMAIL_CREDENTIALS_FILE_PATH")
        if (credentialsPath.isNullOrEmpty()) {
            System.err.println("警告: GMAIL_CREDENTIALS_FILE_PATH 環境變數未設定")
        }

        // 初始化 GmailOperations
        gmailOperations = GmailOperations()
    }

    @Test
    @Order(1)
    fun testSendEmail() {
        // 建立隨機主題以便識別
        val uniqueSubject = "測試郵件 ${UUID.randomUUID()}"
        val body = "這是一封測試郵件的內容，用於驗證 Gmail 操作功能。"

        val result = gmailOperations.sendEmail(testEmailAddress, uniqueSubject, body)
        val response = parseResponse(result)

        // 驗證回應
        assertTrue(response.success, "寄送郵件失敗: ${response.error}")
        assertNotNull(response.data["messageId"], "郵件 ID 不應為空")
        assertNotNull(response.data["threadId"], "執行緒 ID 不應為空")

        // 儲存郵件 ID 以供後續測試使用
        testMessageId = response.data["messageId"] as String
        println("已發送測試郵件，ID: $testMessageId")

        // 等待一下，確保郵件已正確處理
        Thread.sleep(2000)
    }

    @Test
    @Order(2)
    fun testSendEmailWithCC() {
        // 建立隨機主題以便識別
        val uniqueSubject = "測試副本郵件 ${UUID.randomUUID()}"
        val body = "這是一封帶有副本的測試郵件內容，用於驗證 Gmail 副本功能。"
        val cc = testEmailAddress // 將副本發送給自己進行測試

        val result = gmailOperations.sendEmail(testEmailAddress, uniqueSubject, body, cc)
        val response = parseResponse(result)

        // 驗證回應
        assertTrue(response.success, "寄送帶副本郵件失敗: ${response.error}")
        assertNotNull(response.data["messageId"], "郵件 ID 不應為空")
        assertNotNull(response.data["threadId"], "執行緒 ID 不應為空")
        assertEquals(cc, response.data["cc"], "副本收件人應正確設置")

        println("已發送帶副本的測試郵件，ID: ${response.data["messageId"]}")

        // 等待一下，確保郵件已正確處理
        Thread.sleep(2000)
    }

    @Test
    @Order(3)
    fun testSearchEmails() {
        // 先確保已有測試郵件的主題
        val result = gmailOperations.searchEmails("subject:測試郵件", 5)
        val response = parseResponse(result)

        // 驗證回應
        assertTrue(response.success, "搜尋郵件失敗: ${response.error}")

        val emails = response.data["emails"] as List<*>
        assertTrue(emails.isNotEmpty(), "應該找到至少一封測試郵件")

        println("搜尋到 ${emails.size} 封測試郵件")
    }

    @Test
    @Order(4)
    fun testGetEmail() {
        // 確保有測試郵件 ID
        if (testMessageId == null) {
            testSearchEmails() // 如果沒有測試郵件，嘗試搜尋並取得第一封
            // 從搜尋結果中提取第一封郵件的 ID
            val searchResult = gmailOperations.searchEmails("subject:測試郵件", 1)
            val response = parseResponse(searchResult)
            if (response.success) {
                val emails = response.data["emails"] as List<*>
                if (emails.isNotEmpty()) {
                    val firstEmail = emails[0] as Map<*, *>
                    testMessageId = firstEmail["id"] as String
                }
            }
        }

        // 測試獲取郵件詳情
        assertNotNull(testMessageId, "需要有測試郵件的 ID 才能獲取郵件詳情")

        val result = gmailOperations.getEmail(testMessageId!!)
        val response = parseResponse(result)

        // 驗證回應
        assertTrue(response.success, "獲取郵件詳情失敗: ${response.error}")

        val email = response.data["email"] as Map<*, *>
        assertEquals(testMessageId, email["id"], "返回的郵件 ID 應與請求的 ID 匹配")

        println("成功獲取郵件詳情，主題: ${email["subject"]}")
    }

    @Test
    @Order(5)
    fun testMarkAsReadAndUnread() {
        // 確保有測試郵件 ID
        if (testMessageId == null) {
            // 如果沒有測試郵件 ID，從測試獲取郵件詳情
            testGetEmail()
        }

        assertNotNull(testMessageId, "需要有測試郵件的 ID 才能標記已讀/未讀")

        // 測試標記為已讀
        var result = gmailOperations.markAsRead(testMessageId!!)
        var response = parseResponse(result)
        assertTrue(response.success, "標記郵件為已讀失敗: ${response.error}")
        println("郵件已標記為已讀")

        // 等待一下
        Thread.sleep(1000)

        // 測試標記為未讀
        result = gmailOperations.markAsUnread(testMessageId!!)
        response = parseResponse(result)
        assertTrue(response.success, "標記郵件為未讀失敗: ${response.error}")
        println("郵件已標記為未讀")
    }

    @Test
    @Order(6)
    fun testListUnreadEmails() {
        val result = gmailOperations.listUnreadEmails(5)
        val response = parseResponse(result)

        // 驗證回應
        assertTrue(response.success, "列出未讀郵件失敗: ${response.error}")

        val emails = response.data["emails"] as List<*>
        println("獲取到 ${emails.size} 封未讀郵件")
    }

    @Test
    @Order(7)
    @Disabled("這個測試會將郵件移至垃圾桶，預設為停用")
    fun testDeleteEmail() {
        // 確保有測試郵件 ID
        if (testMessageId == null) {
            // 如果沒有測試郵件 ID，從測試獲取郵件詳情
            testGetEmail()
        }

        assertNotNull(testMessageId, "需要有測試郵件的 ID 才能刪除")

        // 測試刪除
        val result = gmailOperations.deleteEmail(testMessageId!!)
        val response = parseResponse(result)

        // 驗證回應
        assertTrue(response.success, "刪除郵件失敗: ${response.error}")
        println("郵件已移至垃圾桶，ID: $testMessageId")
    }

    @Test
    @Order(8)
    @Disabled("需要準備實際存在的附件檔案才能執行")
    fun testSendEmailWithAttachments() {
        val uniqueSubject = "測試附件郵件 ${UUID.randomUUID()}"
        val body = "這是一封帶有附件的測試郵件內容，用於驗證 Gmail 附件功能。"

        val attachments = """
        [
          {"path": "D:/tmp/Test.java", "name": "Test.java"},
          {"path": "D:/tmp/same_file.txt", "name": "same_file.txt"}
        ]
    """.trimIndent()

        val result = gmailOperations.sendEmail(testEmailAddress, uniqueSubject, body, "", attachments)
        val response = parseResponse(result)

        assertTrue(response.success, "寄送帶附件郵件失敗: ${response.error}")
        assertNotNull(response.data["messageId"], "郵件 ID 不應為空")

        // 將附件數量轉換為整數再比較
        val attachmentsCount = (response.data["attachmentsCount"] as Number).toInt()
        assertEquals(2, attachmentsCount, "附件數量應為2")

        println("已發送帶附件的測試郵件，ID: ${response.data["messageId"]}")
    }

    // 解析 JSON 回應為方便處理的物件
    private fun parseResponse(jsonResponse: String): GmailResponse {
        val mapType = object : TypeToken<Map<String, Any?>>() {}.type
        val response = gson.fromJson<Map<String, Any?>>(jsonResponse, mapType)

        return GmailResponse(
            success = response["success"] as Boolean,
            data = (response.filter { it.key != "success" && it.key != "error" }).toMutableMap(),
            error = response["error"] as? String
        )
    }

    // 用於處理 JSON 回應的輔助類
    data class GmailResponse(
        val success: Boolean,
        val data: MutableMap<String, Any?>,
        val error: String? = null
    )
}
