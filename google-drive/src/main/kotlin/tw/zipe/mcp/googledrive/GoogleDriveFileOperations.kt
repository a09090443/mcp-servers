package tw.zipe.mcp.googledrive

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.FileContent
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.gson.Gson
import io.quarkiverse.mcp.server.Tool
import io.quarkiverse.mcp.server.ToolArg
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import org.apache.commons.io.IOUtils

/**
 * Google Drive 文件操作封裝類
 * @author Gary
 * @created 2025/3/28
 */
class GoogleDriveFileOperations {
    private companion object {
        private const val APPLICATION_NAME = "Google Drive File Operations"
        private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
        private const val TOKENS_DIRECTORY_PATH = "tokens"
        private val SCOPES = listOf(DriveScopes.DRIVE_FILE)
        private const val FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
        private val gson = Gson()

        // MIME 類型映射表，用於文件類型猜測
        private val MIME_TYPE_MAP = mapOf(
            ".txt" to "text/plain",
            ".html" to "text/html",
            ".css" to "text/css",
            ".js" to "application/javascript",
            ".json" to "application/json",
            ".xml" to "application/xml",
            ".csv" to "text/csv",
            ".pdf" to "application/pdf",
            ".doc" to "application/msword",
            ".docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            ".xls" to "application/vnd.ms-excel",
            ".xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            ".ppt" to "application/vnd.ms-powerpoint",
            ".pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            ".jpg" to "image/jpeg",
            ".jpeg" to "image/jpeg",
            ".png" to "image/png",
            ".gif" to "image/gif",
            ".bmp" to "image/bmp",
            ".mp3" to "audio/mpeg",
            ".mp4" to "video/mp4",
            ".mpeg" to "video/mpeg",
            ".zip" to "application/zip",
            ".rar" to "application/x-rar-compressed",
            ".tar" to "application/x-tar",
            ".gz" to "application/gzip"
        )
    }

    private val driveService: Drive

    init {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        driveService = Drive.Builder(
            httpTransport,
            JSON_FACTORY,
            getCredentials(httpTransport)
        ).setApplicationName(APPLICATION_NAME).build()
    }

    // 擴展函數：生成成功響應 JSON
    private fun Map<String, Any?>.toSuccessResponse(): String {
        val responseMap = mutableMapOf<String, Any?>("success" to true)
        responseMap.putAll(this)
        return gson.toJson(responseMap)
    }

    // 擴展函數：生成錯誤響應 JSON
    private fun String.toErrorResponse(data: Map<String, Any?> = emptyMap()): String {
        val responseMap = mutableMapOf<String, Any?>("success" to false, "error" to this)
        responseMap.putAll(data)
        return gson.toJson(responseMap)
    }

    // 獲取認證憑據
    private fun getCredentials(httpTransport: HttpTransport): Credential {
        try {
            // 加載客戶端密鑰
            val jsonFile = System.getenv("CREDENTIALS_FILE_PATH")
                ?: throw IllegalArgumentException("CREDENTIALS_FILE_PATH environment variable is not set.")

            val credentialFile = java.io.File(jsonFile)
            credentialFile.inputStream().use { stream ->
                val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(stream))

                // 構建授權流程
                val flow = GoogleAuthorizationCodeFlow.Builder(
                    httpTransport,
                    JSON_FACTORY,
                    clientSecrets,
                    SCOPES
                )
                    .setDataStoreFactory(FileDataStoreFactory(java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build()

                val receiver = LocalServerReceiver.Builder().setPort(8888).build()
                return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
            }
        } catch (e: Exception) {
            throw RuntimeException("無法獲取Google Drive認證", e)
        }
    }

    // 檢查項目是否為目錄
    private fun isDirectory(fileId: String): Boolean {
        return try {
            val file = driveService.files()[fileId].execute()
            file.mimeType == FOLDER_MIME_TYPE
        } catch (e: Exception) {
            false
        }
    }

    // 獲取文件信息
    private fun getFileInfo(fileId: String, fields: String = "id,name,mimeType"): File {
        return driveService.files().get(fileId).setFields(fields).execute()
    }

    // 通用重命名功能
    private fun rename(fileId: String, newName: String): File {
        val fileMetadata = File().setName(newName)
        return driveService.files().update(fileId, fileMetadata).execute()
    }

    // 執行需要錯誤處理的操作
    private inline fun <T> executeWithErrorHandling(
        errorContext: Map<String, Any?> = emptyMap(),
        operation: () -> T
    ): String {
        return try {
            val result = operation()
            when (result) {
                is Map<*, *> -> (result as Map<String, Any?>).toSuccessResponse()
                is String -> mapOf("result" to result).toSuccessResponse()
                else -> mapOf("result" to result).toSuccessResponse()
            }
        } catch (e: Exception) {
            (e.message ?: "操作失敗").toErrorResponse(errorContext)
        }
    }

    @Tool(description = "Create a new file in Google Drive, optionally in a specific directory")
    fun createNewFile(
        @ToolArg(description = "File name") fileName: String,
        @ToolArg(description = "File content, can be empty") content: String = "",
        @ToolArg(description = "File type, such as: text/plain") mimeType: String = "text/plain",
        @ToolArg(description = "Parent directory ID, leave empty for root directory") parentId: String? = null
    ): String = executeWithErrorHandling(mapOf("fileName" to fileName)) {
        // 設置文件元數據
        val fileMetadata = File().setName(fileName)

        // 如果提供了 parentId，則將其設為父目錄
        if (!parentId.isNullOrEmpty()) {
            fileMetadata.parents = listOf(parentId)
        }

        // 將內容轉換為字節數組
        val contentBytes = content.toByteArray(StandardCharsets.UTF_8)
        val mediaContent = com.google.api.client.http.ByteArrayContent(mimeType, contentBytes)

        // 上傳文件到 Google Drive
        val uploadedFile = driveService.files().create(fileMetadata, mediaContent)
            .setFields("id, name, parents")
            .execute()

        mapOf(
            "fileId" to uploadedFile.id,
            "fileName" to uploadedFile.name,
            "parentId" to (uploadedFile.parents?.firstOrNull() ?: "root")
        )
    }

    @Tool(description = "Upload a file to Google Drive")
    fun uploadFile(
        @ToolArg(description = "Local file path") filePath: String,
        @ToolArg(description = "File name in Google Drive") fileName: String,
        @ToolArg(description = "File type, such as: text/plain") fileType: String
    ): String = executeWithErrorHandling(mapOf("filePath" to filePath)) {
        val fileMetadata = File().setName(fileName)
        val file = java.io.File(filePath)
        val mediaContent = FileContent(fileType, file)

        val uploadedFile = driveService.files().create(fileMetadata, mediaContent)
            .setFields("id, name")
            .execute()

        mapOf(
            "fileId" to uploadedFile.id,
            "fileName" to uploadedFile.name
        )
    }

    @Tool(description = "Delete a file from Google Drive")
    fun deleteFile(
        @ToolArg(description = "Google Drive file ID") fileId: String
    ): String = executeWithErrorHandling(mapOf("fileId" to fileId)) {
        driveService.files().delete(fileId).execute()
        mapOf(
            "message" to "File successfully deleted",
            "fileId" to fileId
        )
    }

    @Tool(description = "Read file content from Google Drive")
    fun readFile(
        @ToolArg(description = "Google Drive file ID") fileId: String
    ): String = executeWithErrorHandling(mapOf("fileId" to fileId)) {
        val fileInfo = getFileInfo(fileId, "id,name,mimeType")

        driveService.files()[fileId].executeMediaAsInputStream().use { inputStream ->
            // 根據檔案類型處理內容
            val content = when {
                fileInfo.mimeType.run {
                    contains("text/") || contains("application/json") || contains("application/xml")
                } -> IOUtils.toString(inputStream, StandardCharsets.UTF_8)

                else -> "[Binary file - Please use download function]" // 二進位檔案返回提示資訊
            }

            mapOf(
                "fileId" to fileId,
                "fileName" to fileInfo.name,
                "mimeType" to fileInfo.mimeType,
                "content" to content,
                "encoding" to "UTF-8"
            )
        }
    }

    @Tool(description = "Update file content in Google Drive")
    fun updateFile(
        @ToolArg(description = "Google Drive file ID") fileId: String,
        @ToolArg(description = "New file content as string") content: String,
        @ToolArg(description = "File type, such as: text/plain") fileType: String = "text/plain"
    ): String = executeWithErrorHandling(mapOf("fileId" to fileId)) {
        val fileMetadata = File()
        val contentBytes = content.toByteArray(StandardCharsets.UTF_8)
        val mediaContent = com.google.api.client.http.ByteArrayContent(fileType, contentBytes)

        val updatedFile = driveService.files().update(fileId, fileMetadata, mediaContent)
            .setFields("id,name")
            .execute()

        mapOf(
            "message" to "File content updated successfully",
            "fileId" to updatedFile.id,
            "fileName" to updatedFile.name
        )
    }

    @Tool(description = "Update file content in Google Drive using local file")
    fun updateFileFromLocal(
        @ToolArg(description = "Google Drive file ID") fileId: String,
        @ToolArg(description = "Local file path to upload") filePath: String,
        @ToolArg(description = "File type, such as: text/plain") fileType: String
    ): String = executeWithErrorHandling(mapOf("fileId" to fileId, "filePath" to filePath)) {
        val file = java.io.File(filePath)
        val mediaContent = FileContent(fileType, file)

        val updatedFile = driveService.files().update(fileId, File(), mediaContent)
            .setFields("id,name")
            .execute()

        mapOf(
            "message" to "File content updated successfully from local file",
            "fileId" to updatedFile.id,
            "fileName" to updatedFile.name
        )
    }

    @Tool(description = "List all files in Google Drive")
    fun listFiles(): String = executeWithErrorHandling {
        val result = driveService.files().list()
            .setPageSize(10)
            .setQ("trashed = false")
            .setFields("nextPageToken, files(id, name, mimeType)")
            .execute()

        val files = result.files.map { file ->
            mapOf(
                "id" to file.id,
                "name" to file.name,
                "type" to if (file.mimeType == FOLDER_MIME_TYPE) "Directory" else "File"
            )
        }

        mapOf(
            "files" to files,
            "totalCount" to files.size
        )
    }

    @Tool(description = "Get file ID by file name in Google Drive")
    fun getFileIdByName(
        @ToolArg(description = "File name in Google Drive") fileName: String
    ): String = executeWithErrorHandling(mapOf("fileName" to fileName)) {
        val result = driveService.files().list()
            .setQ("name = '$fileName' and trashed = false")
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()

        val files = result.files
        if (!files.isNullOrEmpty()) {
            mapOf(
                "fileId" to files[0].id,
                "fileName" to files[0].name
            )
        } else {
            throw Exception("Could not find file named '$fileName'")
        }
    }

    @Tool(description = "Search files in Google Drive and return file ID list")
    fun searchFiles(
        @ToolArg(description = "Search keyword") query: String
    ): String = executeWithErrorHandling(mapOf("query" to query)) {
        val result = driveService.files().list()
            .setQ("name contains '$query' and trashed = false")
            .setSpaces("drive")
            .setFields("files(id, name, mimeType)")
            .execute()

        val files = result.files.map {
            mapOf(
                "id" to it.id,
                "name" to it.name,
                "type" to if (it.mimeType == FOLDER_MIME_TYPE) "Directory" else "File"
            )
        }

        mapOf(
            "files" to files,
            "totalCount" to files.size,
            "query" to query
        )
    }

    @Tool(description = "Get file details by file ID")
    fun getFileById(
        @ToolArg(description = "Google Drive file ID") fileId: String
    ): String = executeWithErrorHandling(mapOf("fileId" to fileId)) {
        val file = driveService.files().get(fileId)
            .setFields("id, name, mimeType, size, createdTime, modifiedTime, parents")
            .execute()

        mapOf(
            "fileId" to file.id,
            "fileName" to file.name,
            "mimeType" to file.mimeType,
            "type" to if (file.mimeType == FOLDER_MIME_TYPE) "Directory" else "File",
            "size" to file.getSize(),
            "created" to file.createdTime,
            "modified" to file.modifiedTime,
            "parents" to file.parents
        )
    }

    @Tool(description = "Change file name in Google Drive")
    fun renameFile(
        @ToolArg(description = "Google Drive file ID") fileId: String,
        @ToolArg(description = "New name") newName: String
    ): String = executeWithErrorHandling(mapOf("fileId" to fileId, "newName" to newName)) {
        val renamedFile = rename(fileId, newName)
        mapOf(
            "message" to "File renamed successfully",
            "fileId" to fileId,
            "newName" to newName,
            "oldName" to renamedFile.name
        )
    }

    @Tool(description = "Create directory in Google Drive root")
    fun createDirectory(
        @ToolArg(description = "Directory name") directoryName: String
    ): String = executeWithErrorHandling(mapOf("directoryName" to directoryName)) {
        val fileMetadata = File().setName(directoryName).setMimeType(FOLDER_MIME_TYPE)
        val createdDir = driveService.files().create(fileMetadata)
            .setFields("id, name")
            .execute()

        mapOf(
            "message" to "Directory created successfully",
            "directoryId" to createdDir.id,
            "directoryName" to createdDir.name
        )
    }

    @Tool(description = "Create directory in a specified parent directory in Google Drive")
    fun createDirectoryInParent(
        @ToolArg(description = "Directory name") directoryName: String,
        @ToolArg(description = "Parent directory ID") parentId: String
    ): String = executeWithErrorHandling(mapOf("directoryName" to directoryName, "parentId" to parentId)) {
        val fileMetadata = File().setName(directoryName).setMimeType(FOLDER_MIME_TYPE)
            .setParents(listOf(parentId))

        val createdDir = driveService.files().create(fileMetadata)
            .setFields("id, name")
            .execute()

        mapOf(
            "message" to "Subdirectory created successfully",
            "directoryId" to createdDir.id,
            "directoryName" to createdDir.name,
            "parentId" to parentId
        )
    }

    @Tool(description = "Delete a directory from Google Drive")
    fun deleteDirectory(
        @ToolArg(description = "Google Drive directory ID") directoryId: String
    ): String = executeWithErrorHandling(mapOf("directoryId" to directoryId)) {
        if (!isDirectory(directoryId)) {
            throw IllegalArgumentException("ID $directoryId is not a directory, cannot execute directory deletion")
        }

        driveService.files().delete(directoryId).execute()
        mapOf(
            "message" to "Directory deleted successfully",
            "directoryId" to directoryId
        )
    }

    @Tool(description = "Rename a directory in Google Drive")
    fun renameDirectory(
        @ToolArg(description = "Google Drive directory ID") directoryId: String,
        @ToolArg(description = "New directory name") newName: String
    ): String = executeWithErrorHandling(mapOf("directoryId" to directoryId, "newName" to newName)) {
        if (!isDirectory(directoryId)) {
            throw IllegalArgumentException("ID $directoryId is not a directory, cannot execute directory rename operation")
        }

        val renamedDir = rename(directoryId, newName)
        mapOf(
            "message" to "Directory renamed successfully",
            "directoryId" to directoryId,
            "newName" to newName,
            "oldName" to renamedDir.name
        )
    }

    @Tool(description = "List files and subdirectories in a Google Drive directory")
    fun listDirectoryContents(
        @ToolArg(description = "Google Drive directory ID, empty value means root directory") directoryId: String? = null
    ): String = executeWithErrorHandling(mapOf("directoryId" to (directoryId ?: "root"))) {
        val query = if (directoryId.isNullOrEmpty()) {
            "'root' in parents and trashed = false"
        } else {
            "'$directoryId' in parents and trashed = false"
        }

        val result = driveService.files().list()
            .setQ(query)
            .setFields("files(id, name, mimeType)")
            .execute()

        val contents = result.files.map {
            val type = if (it.mimeType == FOLDER_MIME_TYPE) "Directory" else "File"
            mapOf(
                "id" to it.id,
                "name" to it.name,
                "type" to type
            )
        }

        mapOf(
            "directoryId" to (directoryId ?: "root"),
            "contents" to contents,
            "totalCount" to contents.size
        )
    }

    @Tool(description = "Move a file to a specified directory")
    fun moveFile(
        @ToolArg(description = "Google Drive file ID") fileId: String,
        @ToolArg(description = "Target directory ID") targetDirId: String
    ): String = executeWithErrorHandling(mapOf("fileId" to fileId, "targetDirectoryId" to targetDirId)) {
        // 獲取文件的當前父目錄
        val file = getFileInfo(fileId, "id,name,parents")
        val previousParents = file.parents.joinToString(",")

        // 從所有父目錄中移除並添加到新目錄
        val updatedFile = driveService.files().update(fileId, null)
            .setAddParents(targetDirId)
            .setRemoveParents(previousParents)
            .setFields("id, parents, name")
            .execute()

        mapOf(
            "message" to "File moved successfully",
            "fileId" to fileId,
            "fileName" to updatedFile.name,
            "targetDirectoryId" to targetDirId,
            "previousParents" to previousParents
        )
    }

    @Tool(description = "Search files in a specific Google Drive directory")
    fun searchFilesInDirectory(
        @ToolArg(description = "Search keyword") query: String,
        @ToolArg(description = "Directory ID to search in, empty for root directory") directoryId: String? = null,
        @ToolArg(description = "Whether to search in subdirectories recursively") recursive: Boolean = false
    ): String = executeWithErrorHandling(mapOf("query" to query, "directoryId" to (directoryId ?: "root"))) {
        // 構建查詢條件
        var queryString = "name contains '$query' and trashed = false"

        // 如果不進行遞歸搜索且指定了目錄，添加父目錄條件
        if (!recursive) {
            if (!directoryId.isNullOrEmpty()) {
                queryString += " and '$directoryId' in parents"
            } else {
                queryString += " and 'root' in parents"
            }
        }

        val result = driveService.files().list()
            .setQ(queryString)
            .setSpaces("drive")
            .setFields("files(id, name, mimeType, parents)")
            .execute()

        val files = result.files.map {
            mapOf(
                "id" to it.id,
                "name" to it.name,
                "type" to if (it.mimeType == FOLDER_MIME_TYPE) "Directory" else "File",
                "parentId" to (it.parents?.firstOrNull() ?: "root")
            )
        }

        mapOf(
            "files" to files,
            "totalCount" to files.size,
            "query" to query,
            "directoryId" to (directoryId ?: "root"),
            "recursive" to recursive
        )
    }

    @Tool(description = "Check if a file with specified name exists in a directory")
    fun checkFileExistsInDirectory(
        @ToolArg(description = "Directory ID") directoryId: String,
        @ToolArg(description = "File name to check") fileName: String
    ): String = executeWithErrorHandling(mapOf("directoryId" to directoryId, "fileName" to fileName)) {
        // 構建查詢：在指定目錄中查找有特定名稱且不在垃圾桶中的文件
        val query = "'$directoryId' in parents and name = '$fileName' and trashed = false"

        val result = driveService.files().list()
            .setQ(query)
            .setFields("files(id, name)")
            .execute()

        val files = result.files
        val exists = files != null && files.isNotEmpty()

        mapOf(
            "exists" to exists,
            "directoryId" to directoryId,
            "fileName" to fileName,
            "fileId" to if (exists) files[0].id else null
        )
    }

    @Tool(description = "Search directories in Google Drive by name")
    fun searchDirectories(
        @ToolArg(description = "Directory name or keyword to search") query: String,
        @ToolArg(description = "Whether to perform exact match (true) or partial match (false)") exactMatch: Boolean = false
    ): String = executeWithErrorHandling(mapOf("query" to query, "exactMatch" to exactMatch)) {
        // 建立查詢條件
        val queryString = if (exactMatch) {
            "mimeType = '$FOLDER_MIME_TYPE' and name = '$query' and trashed = false"
        } else {
            "mimeType = '$FOLDER_MIME_TYPE' and name contains '$query' and trashed = false"
        }

        val result = driveService.files().list()
            .setQ(queryString)
            .setSpaces("drive")
            .setFields("files(id, name, parents)")
            .execute()

        val directories = result.files.map {
            mapOf(
                "id" to it.id,
                "name" to it.name,
                "parentId" to (it.parents?.firstOrNull() ?: "root")
            )
        }

        mapOf(
            "directories" to directories,
            "totalCount" to directories.size,
            "query" to query,
            "exactMatch" to exactMatch
        )
    }

    @Tool(description = "Download a file from Google Drive to local storage")
    fun downloadFile(
        @ToolArg(description = "Google Drive file ID") fileId: String,
        @ToolArg(description = "Local file path to save the downloaded file") localFilePath: String
    ): String = executeWithErrorHandling(mapOf("fileId" to fileId, "localPath" to localFilePath)) {
        // 獲取檔案資訊
        val fileInfo = getFileInfo(fileId, "id,name,mimeType")

        // 下載檔案內容
        FileOutputStream(java.io.File(localFilePath)).use { outputStream ->
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
        }

        // 檢查檔案大小
        val downloadedFile = java.io.File(localFilePath)
        val fileSize = downloadedFile.length()

        mapOf(
            "message" to "File downloaded successfully",
            "fileId" to fileId,
            "fileName" to fileInfo.name,
            "mimeType" to fileInfo.mimeType,
            "localPath" to localFilePath,
            "fileSize" to fileSize
        )
    }

    @Tool(description = "Upload a file to a specific directory in Google Drive")
    fun uploadFileToDirectory(
        @ToolArg(description = "Local file path") filePath: String,
        @ToolArg(description = "File name in Google Drive (optional, uses local filename if empty)") fileName: String = "",
        @ToolArg(description = "Google Drive directory ID to upload to, empty for root") directoryId: String = "",
        @ToolArg(description = "File MIME type (optional, will be guessed if empty)") mimeType: String = ""
    ): String = executeWithErrorHandling(mapOf("localPath" to filePath, "directoryId" to directoryId)) {
        val localFile = java.io.File(filePath)

        // 決定檔案名稱和MIME類型
        val targetFileName = fileName.takeIf { it.isNotBlank() } ?: localFile.name
        val targetMimeType = if (mimeType.isBlank()) guessFileMimeType(targetFileName) else mimeType

        // 設定檔案元數據
        val fileMetadata = File().setName(targetFileName).apply {
            if (directoryId.isNotEmpty()) {
                parents = listOf(directoryId)
            }
        }

        val mediaContent = FileContent(targetMimeType, localFile)

        // 上傳檔案
        val uploadedFile = driveService.files().create(fileMetadata, mediaContent)
            .setFields("id, name, mimeType, size, parents")
            .execute()

        mapOf(
            "message" to "File uploaded successfully",
            "fileId" to uploadedFile.id,
            "fileName" to uploadedFile.name,
            "mimeType" to uploadedFile.mimeType,
            "fileSize" to uploadedFile.getSize(),
            "parentId" to (uploadedFile.parents?.firstOrNull() ?: "root"),
            "localPath" to filePath
        )
    }

    // 幫助函數：根據檔案名稱猜測 MIME 類型
    private fun guessFileMimeType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").let { if (it == fileName) "" else ".$it" }.lowercase()
        return MIME_TYPE_MAP[extension] ?: "application/octet-stream"
    }
}
