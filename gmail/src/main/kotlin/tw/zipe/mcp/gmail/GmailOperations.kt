package tw.zipe.mcp.gmail

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.ListMessagesResponse
import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.model.MessagePart
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.quarkiverse.mcp.server.Tool
import io.quarkiverse.mcp.server.ToolArg
import jakarta.mail.MessagingException
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStreamReader
import java.util.Base64
import java.util.Properties

class GmailOperations {
    private companion object {
        private const val APPLICATION_NAME = "Gmail Operations"
        private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
        private const val TOKENS_DIRECTORY_PATH = "gmail_tokens"
        private val SCOPES = listOf(
            GmailScopes.GMAIL_SEND,
            GmailScopes.GMAIL_READONLY,
            GmailScopes.GMAIL_MODIFY
        )
        private val gson = Gson()
    }

    private val gmail: Gmail

    init {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        gmail = Gmail.Builder(
            httpTransport,
            JSON_FACTORY,
            getCredentials(httpTransport)
        )
            .setApplicationName(APPLICATION_NAME)
            .build()
    }

    // Shared method: Generate success response JSON
    private fun createSuccessResponse(data: Map<String, Any?>): String {
        val responseMap = mutableMapOf<String, Any?>("success" to true)
        responseMap.putAll(data)
        return gson.toJson(responseMap)
    }

    // Shared method: Generate error response JSON
    private fun createErrorResponse(error: String, data: Map<String, Any?> = emptyMap()): String {
        val responseMap = mutableMapOf<String, Any?>("success" to false, "error" to error)
        responseMap.putAll(data)
        return gson.toJson(responseMap)
    }

    private fun getCredentials(httpTransport: HttpTransport): Credential {
        // Load client secrets
        val jsonFile = System.getenv("GMAIL_CREDENTIALS_FILE_PATH")
        require(!jsonFile.isNullOrEmpty()) { "GMAIL_CREDENTIALS_FILE_PATH environment variable is not set." }

        val credentialFile = File(jsonFile)
        val inputStream = credentialFile.inputStream()
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))

        // Build authorization flow
        val flow = GoogleAuthorizationCodeFlow.Builder(
            httpTransport,
            JSON_FACTORY,
            clientSecrets,
            SCOPES
        )
            .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build()

        val receiver = LocalServerReceiver.Builder().setPort(8889).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    // 建立帶附件和副本的MIME郵件
    private fun createEmail(
        to: String,
        subject: String,
        bodyText: String,
        cc: String? = "",
        attachments: List<Map<String, String>> = emptyList()
    ): MimeMessage {
        val props = Properties()
        val session = Session.getDefaultInstance(props, null)

        val email = MimeMessage(session)
        email.setFrom(InternetAddress("me"))
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, InternetAddress(to))

        // 添加副本
        if (!cc.isNullOrEmpty()) {
            email.addRecipient(jakarta.mail.Message.RecipientType.CC, InternetAddress(cc))
        }

        email.subject = subject

        // 建立多部分郵件
        if (attachments.isEmpty()) {
            // 如果沒有附件，直接設置文本內容
            email.setText(bodyText)
        } else {
            // 有附件，建立多部分郵件
            val multipart = jakarta.mail.internet.MimeMultipart()

            // 添加文本部分
            val textPart = jakarta.mail.internet.MimeBodyPart()
            textPart.setText(bodyText)
            multipart.addBodyPart(textPart)

            // 添加附件
            for (attachment in attachments) {
                val filePath = attachment["path"] ?: continue
                val fileName = attachment["name"] ?: File(filePath).name

                val attachmentPart = jakarta.mail.internet.MimeBodyPart()
                val file = File(filePath)
                if (!file.exists()) {
                    throw MessagingException("附件檔案不存在: $filePath")
                }

                attachmentPart.attachFile(file)
                attachmentPart.fileName = fileName
                multipart.addBodyPart(attachmentPart)
            }

            // 設置郵件內容為多部分內容
            email.setContent(multipart)
        }

        return email
    }

    // 轉換MimeMessage為Gmail API消息格式
    private fun createMessageWithEmail(emailContent: MimeMessage): Message {
        val buffer = ByteArrayOutputStream()
        emailContent.writeTo(buffer)
        val bytes = buffer.toByteArray()
        // 使用新的Base64編碼方式
        val encodedEmail = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        val message = Message()
        message.raw = encodedEmail
        return message
    }

    // 獲取郵件內容
    private fun getEmailContent(message: Message): Map<String, Any?> {
        val fullMessage = gmail.users().messages().get("me", message.id).setFormat("full").execute()
        val headers = fullMessage.payload.headers

        val subject = headers.find { it.name == "Subject" }?.value ?: ""
        val from = headers.find { it.name == "From" }?.value ?: ""
        val date = headers.find { it.name == "Date" }?.value ?: ""
        val to = headers.find { it.name == "To" }?.value ?: ""

        val content = extractContent(fullMessage.payload)

        return mapOf(
            "id" to fullMessage.id,
            "threadId" to fullMessage.threadId,
            "labelIds" to fullMessage.labelIds,
            "snippet" to fullMessage.snippet,
            "subject" to subject,
            "from" to from,
            "to" to to,
            "date" to date,
            "content" to content
        )
    }

    // 遞迴提取郵件內容
    private fun extractContent(messagePart: MessagePart): String {
        return if (messagePart.mimeType == "text/plain" && messagePart.body.data != null) {
            // 使用新的Base64解碼方式
            String(Base64.getUrlDecoder().decode(messagePart.body.data))
        } else if (messagePart.parts != null) {
            messagePart.parts.joinToString("\n") { extractContent(it) }
        } else {
            ""
        }
    }

    @Tool(description = "Send email through Gmail with optional attachments and CC")
    fun sendEmail(
        @ToolArg(description = "Email recipient address") to: String,
        @ToolArg(description = "Email subject") subject: String,
        @ToolArg(description = "Email body content") bodyText: String,
        @ToolArg(description = "CC recipients", required = false) cc: String? = null,
        @ToolArg(
            description = "List of attachments with path and name fields",
            required = false
        ) attachments: String = "[]"
    ): String {
        return try {

            val attachmentsList = try {
                gson.fromJson<Array<Map<String, String>>>(
                    attachments,
                    object : TypeToken<Array<Map<String, String>>>() {}.type
                )?.toList() ?: emptyList()
            } catch (e: Exception) {
                return createErrorResponse("無效的附件格式: ${e.message}")
            }

            val email = createEmail(to, subject, bodyText, cc, attachmentsList)
            val message = createMessageWithEmail(email)
            val sentMessage = gmail.users().messages().send("me", message).execute()

            createSuccessResponse(
                mapOf(
                    "messageId" to sentMessage.id,
                    "threadId" to sentMessage.threadId,
                    "recipient" to to,
                    "cc" to cc,
                    "subject" to subject,
                    "attachmentsCount" to attachmentsList.size
                )
            )
        } catch (e: MessagingException) {
            e.printStackTrace()
            createErrorResponse("建立郵件失敗: ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            createErrorResponse("發送郵件失敗: ${e.message}")
        }
    }

    @Tool(description = "List unread emails in your Gmail inbox")
    fun listUnreadEmails(
        @ToolArg(description = "Maximum number of emails to retrieve") maxResults: Int = 10
    ): String {
        return try {
            val result: ListMessagesResponse = gmail.users().messages().list("me")
                .setQ("is:unread in:inbox")
                .setMaxResults(maxResults.toLong())
                .execute()

            val messages = result.messages ?: emptyList()
            val emailList = messages.map { message ->
                getEmailContent(message)
            }

            createSuccessResponse(
                mapOf(
                    "emails" to emailList,
                    "count" to emailList.size,
                    "hasMore" to (result.nextPageToken != null)
                )
            )
        } catch (e: Exception) {
            createErrorResponse("獲取未讀郵件失敗: ${e.message}")
        }
    }

    @Tool(description = "Search for emails in Gmail using query")
    fun searchEmails(
        @ToolArg(description = "Search query, using Gmail search syntax") query: String,
        @ToolArg(description = "Maximum number of emails to retrieve") maxResults: Int = 10
    ): String {
        return try {
            val result: ListMessagesResponse = gmail.users().messages().list("me")
                .setQ(query)
                .setMaxResults(maxResults.toLong())
                .execute()

            val messages = result.messages ?: emptyList()
            val emailList = messages.map { message ->
                getEmailContent(message)
            }

            createSuccessResponse(
                mapOf(
                    "emails" to emailList,
                    "count" to emailList.size,
                    "query" to query,
                    "hasMore" to (result.nextPageToken != null)
                )
            )
        } catch (e: Exception) {
            createErrorResponse("搜尋郵件失敗: ${e.message}")
        }
    }

    @Tool(description = "Get email details by ID")
    fun getEmail(
        @ToolArg(description = "Gmail message ID") messageId: String
    ): String {
        return try {
            val message = gmail.users().messages().get("me", messageId).execute()
            val emailDetails = getEmailContent(message)

            createSuccessResponse(
                mapOf("email" to emailDetails)
            )
        } catch (e: Exception) {
            createErrorResponse("獲取郵件失敗: ${e.message}", mapOf("messageId" to messageId))
        }
    }

    @Tool(description = "Mark email as read")
    fun markAsRead(
        @ToolArg(description = "Gmail message ID") messageId: String
    ): String {
        return try {
            val mods = com.google.api.services.gmail.model.ModifyMessageRequest()
                .setRemoveLabelIds(listOf("UNREAD"))

            gmail.users().messages().modify("me", messageId, mods).execute()

            createSuccessResponse(
                mapOf(
                    "message" to "郵件已標記為已讀",
                    "messageId" to messageId
                )
            )
        } catch (e: Exception) {
            createErrorResponse("標記郵件為已讀失敗: ${e.message}", mapOf("messageId" to messageId))
        }
    }

    @Tool(description = "Mark email as unread")
    fun markAsUnread(
        @ToolArg(description = "Gmail message ID") messageId: String
    ): String {
        return try {
            val mods = com.google.api.services.gmail.model.ModifyMessageRequest()
                .setAddLabelIds(listOf("UNREAD"))

            gmail.users().messages().modify("me", messageId, mods).execute()

            createSuccessResponse(
                mapOf(
                    "message" to "郵件已標記為未讀",
                    "messageId" to messageId
                )
            )
        } catch (e: Exception) {
            createErrorResponse("標記郵件為未讀失敗: ${e.message}", mapOf("messageId" to messageId))
        }
    }

    @Tool(description = "Delete an email")
    fun deleteEmail(
        @ToolArg(description = "Gmail message ID") messageId: String
    ): String {
        return try {
            gmail.users().messages().trash("me", messageId).execute()

            createSuccessResponse(
                mapOf(
                    "message" to "郵件已移至垃圾桶",
                    "messageId" to messageId
                )
            )
        } catch (e: Exception) {
            createErrorResponse("刪除郵件失敗: ${e.message}", mapOf("messageId" to messageId))
        }
    }
}
