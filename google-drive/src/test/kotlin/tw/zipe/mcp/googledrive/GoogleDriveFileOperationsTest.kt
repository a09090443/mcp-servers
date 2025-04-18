package tw.zipe.mcp.googledrive

import com.google.gson.Gson
import java.io.File
import java.io.FileWriter
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Google Drive 文件操作測試類
 * @author Gary
 * @created 2025/3/28
 */
class GoogleDriveFileOperationsTest {
    private lateinit var driveOperations: GoogleDriveFileOperations
    private lateinit var tempTestFile: File
    private val gson = Gson()

    // 追蹤測試資源以便測試後清理
    private val resourceTracker = ResourceTracker()

    @BeforeEach
    fun setup() {
        // 禁用 SSL 驗證以便測試
        SSLUtil.disableSSLVerification()

        // 初始化 Google Drive 操作類
        driveOperations = GoogleDriveFileOperations()

        // 創建臨時測試文件
        val tempPath = kotlin.io.path.createTempFile("test_file_ops", ".txt")
        tempTestFile = tempPath.toFile().apply {
            FileWriter(this).use { it.write("這是用於 Google Drive 操作的測試檔案") }
            deleteOnExit()
        }
    }

    @AfterEach
    fun cleanup() {
        // 清理所有創建的測試資源
        resourceTracker.cleanupResources(driveOperations)
    }

    @Test
    @DisplayName("測試創建及刪除檔案")
    fun testCreateAndDeleteFile() {
        // 創建新檔案
        val fileName = generateUniqueFileName("test_file")
        val content = "這是基本操作測試的檔案內容"

        val createResponse = executeOperation {
            driveOperations.createNewFile(fileName, content)
        }

        assertTrue(createResponse["success"] as Boolean, "檔案創建應該成功")
        val fileId = createResponse["fileId"] as String
        resourceTracker.trackFile(fileId)

        // 刪除檔案
        val deleteResponse = executeOperation {
            driveOperations.deleteFile(fileId)
        }

        assertTrue(deleteResponse["success"] as Boolean, "檔案刪除應該成功")
        resourceTracker.untrackFile(fileId)

        // 確認檔案已刪除
        val readAgainResponse = executeOperation {
            driveOperations.readFile(fileId)
        }

        assertFalse(readAgainResponse["success"] as Boolean, "讀取已刪除的檔案應該失敗")
    }

    @Test
    @DisplayName("測試讀取檔案內容")
    fun testReadFileContent() {
        // 創建新檔案
        val fileName = generateUniqueFileName("read_test_file")
        val content = "這是用於測試讀取功能的檔案內容"

        val createResponse = executeOperation {
            driveOperations.createNewFile(fileName, content)
        }

        assertTrue(createResponse["success"] as Boolean, "檔案創建應該成功")
        val fileId = createResponse["fileId"] as String
        resourceTracker.trackFile(fileId)

        // 讀取檔案內容
        val readResponse = executeOperation {
            driveOperations.readFile(fileId)
        }

        assertTrue(readResponse["success"] as Boolean, "讀取檔案內容應該成功")
        assertEquals(content, readResponse["content"], "檔案內容應該匹配")
        assertEquals(fileName, readResponse["fileName"], "檔案名稱應該匹配")
        assertNotNull(readResponse["mimeType"], "應返回檔案的 MIME 類型")
        assertNotNull(readResponse["encoding"], "應返回編碼資訊")
    }

    @Test
    @DisplayName("測試上傳及更新檔案內容")
    fun testUploadAndUpdateFile() {
        // 上傳測試檔案
        val fileName = generateUniqueFileName("upload_test")

        val uploadResponse = executeOperation {
            driveOperations.uploadFile(
                tempTestFile.absolutePath,
                fileName,
                "text/plain"
            )
        }

        assertTrue(uploadResponse["success"] as Boolean, "檔案上傳應該成功")
        val fileId = uploadResponse["fileId"] as String
        resourceTracker.trackFile(fileId)

        // 讀取上傳的檔案內容
        val readResponse = executeOperation {
            driveOperations.readFile(fileId)
        }
        val originalContent = readResponse["content"] as String

        // 更新檔案內容
        val updatedContent = "這是更新後的內容，用於測試檔案更新功能"
        val updateResponse = executeOperation {
            driveOperations.updateFile(fileId, updatedContent, "text/plain")
        }
        assertTrue(updateResponse["success"] as Boolean, "檔案更新應該成功")

        // 驗證檔案內容已更新
        val readUpdatedResponse = executeOperation {
            driveOperations.readFile(fileId)
        }
        val newContent = readUpdatedResponse["content"] as String

        assertAll(
            { assertNotNull(newContent, "更新後的內容不應為空") },
            { assertEquals(updatedContent, newContent, "更新後的內容應符合我們發送的內容") },
            { assertNotEquals(originalContent, newContent, "更新後的內容應與原始內容不同") }
        )
    }

    @Test
    @DisplayName("測試使用本地檔案更新檔案內容")
    fun testUpdateFileFromLocalFile() {
        // 先創建一個檔案
        val fileName = generateUniqueFileName("file_to_update")
        val createResponse = executeOperation {
            driveOperations.createNewFile(fileName, "初始內容")
        }

        val fileId = createResponse["fileId"] as String
        resourceTracker.trackFile(fileId)

        // 創建一個新的臨時檔案用於更新
        val updatedFile = kotlin.io.path.createTempFile("updated_file", ".txt").toFile().apply {
            FileWriter(this).use { it.write("這是從本地檔案更新的內容") }
            deleteOnExit()
        }

        // 使用本地檔案更新 Google Drive 檔案
        val updateResponse = executeOperation {
            driveOperations.updateFileFromLocal(
                fileId,
                updatedFile.absolutePath,
                "text/plain"
            )
        }
        assertTrue(updateResponse["success"] as Boolean, "從本地檔案更新應該成功")

        // 驗證檔案內容已更新
        val readResponse = executeOperation {
            driveOperations.readFile(fileId)
        }
        val content = readResponse["content"] as String
        assertTrue(content.contains("從本地檔案更新的內容"), "檔案內容應包含本地檔案中的文本")
    }

    @Test
    @DisplayName("測試檔案列表與搜尋")
    fun testFileListingAndSearching() {
        // 創建具有獨特名稱的檔案以便搜尋
        val uniqueFileName = generateUniqueFileName("unique_search_test")
        val createResponse = executeOperation {
            driveOperations.createNewFile(uniqueFileName, "用於搜尋測試的內容")
        }

        val fileId = createResponse["fileId"] as String
        resourceTracker.trackFile(fileId)

        // 測試列出所有檔案
        val listResponse = executeOperation {
            driveOperations.listFiles()
        }

        val files = listResponse["files"] as List<*>
        assertTrue(files.isNotEmpty(), "檔案列表不應為空")

        // 確認創建的檔案在列表中
        val foundInList = files.any { file -> (file as Map<*, *>)["id"] == fileId }
        assertTrue(foundInList, "創建的檔案應出現在檔案列表中")

        // 測試通過名稱搜索檔案
        val searchKeyword = uniqueFileName.substring(0, 10)
        val searchResponse = executeOperation {
            driveOperations.searchFiles(searchKeyword)
        }

        val searchFiles = searchResponse["files"] as List<*>
        assertTrue(searchFiles.isNotEmpty(), "搜尋結果不應為空")

        // 測試通過精確名稱獲取檔案ID
        val getIdResponse = executeOperation {
            driveOperations.getFileIdByName(uniqueFileName)
        }

        assertEquals(fileId, getIdResponse["fileId"], "獲取的檔案ID應匹配創建的檔案ID")
    }

    @Test
    @DisplayName("測試檔案重命名")
    fun testFileRename() {
        // 創建測試檔案
        val originalName = generateUniqueFileName("original_name")
        val createResponse = executeOperation {
            driveOperations.createNewFile(originalName, "用於測試重命名功能的內容")
        }

        val fileId = createResponse["fileId"] as String
        resourceTracker.trackFile(fileId)

        // 重命名檔案
        val newName = generateUniqueFileName("renamed_file")
        val renameResponse = executeOperation {
            driveOperations.renameFile(fileId, newName)
        }
        assertTrue(renameResponse["success"] as Boolean, "重命名檔案應該成功")

        // 驗證檔案已被重命名
        val getFileResponse = executeOperation {
            driveOperations.getFileById(fileId)
        }
        assertEquals(newName, getFileResponse["fileName"], "檔案名應更新為新名稱")

        // 確認舊名稱不再存在
        val getOldIdResponse = executeOperation {
            driveOperations.getFileIdByName(originalName)
        }
        assertFalse(getOldIdResponse["success"] as Boolean, "舊檔案名應不再存在")
    }

    @Test
    @DisplayName("測試目錄操作")
    fun testDirectoryOperations() {
        // 創建測試目錄
        val dirName = generateUniqueDirectoryName("test_dir")
        val createDirResponse = executeOperation {
            driveOperations.createDirectory(dirName)
        }

        val directoryId = createDirResponse["directoryId"] as String
        resourceTracker.trackDirectory(directoryId)

        // 測試創建子目錄
        val subDirName = generateUniqueDirectoryName("test_subdir")
        val createSubDirResponse = executeOperation {
            driveOperations.createDirectoryInParent(subDirName, directoryId)
        }

        val subDirId = createSubDirResponse["directoryId"] as String
        resourceTracker.trackDirectory(subDirId)

        // 驗證子目錄存在於父目錄中
        val listDirResponse = executeOperation {
            driveOperations.listDirectoryContents(directoryId)
        }

        val dirContents = listDirResponse["contents"] as List<*>
        val subDirectoryFound = dirContents.any { content ->
            val contentMap = content as Map<*, *>
            contentMap["id"] == subDirId && contentMap["name"] == subDirName
        }
        assertTrue(subDirectoryFound, "子目錄應出現在父目錄內容中")

        // 測試重命名目錄
        val newDirName = generateUniqueDirectoryName("renamed_dir")
        val renameDirResponse = executeOperation {
            driveOperations.renameDirectory(directoryId, newDirName)
        }
        assertTrue(renameDirResponse["success"] as Boolean, "目錄重命名應該成功")

        // 獲取目錄信息確認改名成功
        val getDirResponse = executeOperation {
            driveOperations.getFileById(directoryId)
        }
        assertEquals(newDirName, getDirResponse["fileName"], "目錄名應該已更新")

        // 刪除子目錄
        val deleteSubDirResponse = executeOperation {
            driveOperations.deleteDirectory(subDirId)
        }
        assertTrue(deleteSubDirResponse["success"] as Boolean, "子目錄刪除應該成功")
        resourceTracker.untrackDirectory(subDirId)

        // 驗證子目錄被成功刪除
        val listAfterDeleteResponse = executeOperation {
            driveOperations.listDirectoryContents(directoryId)
        }

        val contentsAfterDelete = listAfterDeleteResponse["contents"] as List<*>
        val subDirectoryStillExists = contentsAfterDelete.any { content ->
            (content as Map<*, *>)["id"] == subDirId
        }
        assertFalse(subDirectoryStillExists, "刪除後子目錄不應再存在")
    }

    @Test
    @DisplayName("測試目錄與檔案操作整合")
    fun testDirectoryAndFileOperationsIntegration() {
        // 創建測試目錄
        val dirName = generateUniqueDirectoryName("test_dir_files")
        val createDirResponse = executeOperation {
            driveOperations.createDirectory(dirName)
        }

        val directoryId = createDirResponse["directoryId"] as String
        resourceTracker.trackDirectory(directoryId)

        // 創建測試檔案
        val fileName = generateUniqueFileName("test_file_in_dir")
        val createFileResponse = executeOperation {
            driveOperations.createNewFile(fileName, "測試目錄中檔案的內容")
        }

        val fileId = createFileResponse["fileId"] as String
        resourceTracker.trackFile(fileId)

        // 將檔案移動到目錄中
        val moveResponse = executeOperation {
            driveOperations.moveFile(fileId, directoryId)
        }
        assertTrue(moveResponse["success"] as Boolean, "移動檔案到目錄應該成功")

        // 列出目錄內容並確認檔案存在
        val listDirResponse = executeOperation {
            driveOperations.listDirectoryContents(directoryId)
        }

        val dirContents = listDirResponse["contents"] as List<*>
        val fileFound = dirContents.any { item ->
            val fileMap = item as Map<*, *>
            fileMap["id"] == fileId && fileMap["name"] == fileName
        }
        assertTrue(fileFound, "移動後檔案應在目錄中")

        // 測試檢查檔案是否存在
        val checkExistsResponse = executeOperation {
            driveOperations.checkFileExistsInDirectory(directoryId, fileName)
        }
        assertTrue(checkExistsResponse["exists"] as Boolean, "檔案應存在於目錄中")
        assertEquals(fileId, checkExistsResponse["fileId"], "檔案ID應匹配")
    }

    @Test
    @DisplayName("測試根目錄列表")
    fun testRootDirectoryListing() {
        val listRootResponse = executeOperation {
            driveOperations.listDirectoryContents()
        }

        val rootContents = listRootResponse["contents"] as List<*>
        assertNotNull(rootContents, "根目錄內容不應為空")
        assertEquals("root", listRootResponse["directoryId"], "目錄ID應為'root'")
    }

    @Test
    @DisplayName("測試搜尋目錄功能")
    fun testSearchDirectories() {
        // 創建具有獨特名稱的目錄以便搜尋
        val uniqueDirName = generateUniqueDirectoryName("unique_search_dir")
        val createDirResponse = executeOperation {
            driveOperations.createDirectory(uniqueDirName)
        }

        val directoryId = createDirResponse["directoryId"] as String
        resourceTracker.trackDirectory(directoryId)

        // 測試模糊搜尋目錄
        val partialSearchKeyword = uniqueDirName.substring(0, 10)
        val searchPartialResponse = executeOperation {
            driveOperations.searchDirectories(partialSearchKeyword, false)
        }

        val partialResults = searchPartialResponse["directories"] as List<*>
        assertTrue(partialResults.isNotEmpty(), "模糊搜尋結果不應為空")

        // 測試精確匹配搜尋
        val searchExactResponse = executeOperation {
            driveOperations.searchDirectories(uniqueDirName, true)
        }

        val exactResults = searchExactResponse["directories"] as List<*>
        assertEquals(1, exactResults.size, "精確搜尋應返回恰好一個結果")

        val foundDir = exactResults[0] as Map<*, *>
        assertEquals(directoryId, foundDir["id"], "找到的目錄ID應匹配創建的目錄ID")
        assertEquals(uniqueDirName, foundDir["name"], "找到的目錄名稱應匹配")
    }

    @Test
    @DisplayName("測試在指定目錄中搜尋檔案")
    fun testSearchFilesInDirectory() {
        // 創建測試目錄
        val dirName = generateUniqueDirectoryName("dir_for_search")
        val createDirResponse = executeOperation {
            driveOperations.createDirectory(dirName)
        }

        val directoryId = createDirResponse["directoryId"] as String
        resourceTracker.trackDirectory(directoryId)

        // 在目錄中創建兩個檔案，具有相似的名稱
        val baseFileName = "searchable_file_${System.currentTimeMillis()}"
        val fileName1 = "${baseFileName}_1.txt"
        val fileName2 = "${baseFileName}_2.txt"

        // 創建第一個檔案並移動到目錄
        val createFileResponse1 = executeOperation {
            driveOperations.createNewFile(fileName1, "第一個可搜尋檔案的內容")
        }
        val fileId1 = createFileResponse1["fileId"] as String
        resourceTracker.trackFile(fileId1)
        driveOperations.moveFile(fileId1, directoryId)

        // 創建第二個檔案並移動到目錄
        val createFileResponse2 = executeOperation {
            driveOperations.createNewFile(fileName2, "第二個可搜尋檔案的內容")
        }
        val fileId2 = createFileResponse2["fileId"] as String
        resourceTracker.trackFile(fileId2)
        driveOperations.moveFile(fileId2, directoryId)

        // 在特定目錄中搜尋檔案
        val searchKeyword = baseFileName.substring(0, 10)
        val searchResponse = executeOperation {
            driveOperations.searchFilesInDirectory(searchKeyword, directoryId)
        }

        val searchResults = searchResponse["files"] as List<*>
        assertEquals(2, searchResults.size, "搜尋應該找到兩個檔案")

        // 檢查搜尋結果是否包含兩個創建的檔案
        val foundIds = searchResults.map { (it as Map<*, *>)["id"] as String }
        assertTrue(foundIds.contains(fileId1), "搜尋結果應包含第一個檔案")
        assertTrue(foundIds.contains(fileId2), "搜尋結果應包含第二個檔案")
    }

    // 資源追蹤器
    private class ResourceTracker {
        private val fileIds = mutableListOf<String>()
        private val directoryIds = mutableListOf<String>()

        fun trackFile(fileId: String) = fileIds.add(fileId)
        fun untrackFile(fileId: String) = fileIds.remove(fileId)
        fun trackDirectory(directoryId: String) = directoryIds.add(directoryId)
        fun untrackDirectory(directoryId: String) = directoryIds.remove(directoryId)

        fun cleanupResources(driveOperations: GoogleDriveFileOperations) {
            // 先清理檔案，後清理目錄
            fileIds.forEach {
                try {
                    driveOperations.deleteFile(it)
                } catch (ex: Exception) {
                    println("清理檔案失敗 (ID: $it): ${ex.message}")
                }
            }

            directoryIds.forEach {
                try {
                    driveOperations.deleteDirectory(it)
                } catch (ex: Exception) {
                    println("清理目錄失敗 (ID: $it): ${ex.message}")
                }
            }

            fileIds.clear()
            directoryIds.clear()
        }
    }

    // 工具方法
    private fun generateUniqueFileName(prefix: String) = "${prefix}_${System.currentTimeMillis()}.txt"
    private fun generateUniqueDirectoryName(prefix: String) = "${prefix}_${System.currentTimeMillis()}"
    private fun executeOperation(operation: () -> String): Map<*, *> = gson.fromJson(operation(), Map::class.java)
}

// SSL 工具類
class SSLUtil {
    companion object {
        fun disableSSLVerification() {
            try {
                // 創建信任所有證書的信任管理器
                val trustAllCerts = arrayOf(object : javax.net.ssl.X509TrustManager {
                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
                    override fun checkClientTrusted(
                        certs: Array<java.security.cert.X509Certificate>,
                        authType: String
                    ) {
                    }

                    override fun checkServerTrusted(
                        certs: Array<java.security.cert.X509Certificate>,
                        authType: String
                    ) {
                    }
                })

                // 安裝自定義的 SSLSocketFactory
                val sc = javax.net.ssl.SSLContext.getInstance("SSL")
                sc.init(null, trustAllCerts, java.security.SecureRandom())
                javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)

                // 安裝自定義的 HostnameVerifier
                javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
