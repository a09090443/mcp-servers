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
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import org.apache.commons.io.IOUtils

/**
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
    }

    private val driveService: Drive

    init {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        driveService = Drive.Builder(
            httpTransport,
            JSON_FACTORY,
            getCredentials(httpTransport)
        )
            .setApplicationName(APPLICATION_NAME)
            .build()
    }

    // 共用方法: 生成成功響應 JSON
    private fun createSuccessResponse(data: Map<String, Any?>): String {
        val responseMap = mutableMapOf<String, Any?>("success" to true)
        responseMap.putAll(data)
        return gson.toJson(responseMap)
    }

    // 共用方法: 生成錯誤響應 JSON
    private fun createErrorResponse(error: String, data: Map<String, Any?> = emptyMap()): String {
        val responseMap = mutableMapOf<String, Any?>("success" to false, "error" to error)
        responseMap.putAll(data)
        return gson.toJson(responseMap)
    }

    private fun getCredentials(httpTransport: HttpTransport): Credential {
        // 加載客戶端密鑰
        val jsonFile = System.getenv("CREDENTIALS_FILE_PATH")
        require(!jsonFile.isNullOrEmpty()) { "CREDENTIALS_FILE_PATH environment variable is not set." }

        val credentialFile = java.io.File(jsonFile)
        val inputStream = credentialFile.inputStream()
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))

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

    // 共用方法: 檢查項目是否為目錄
    private fun isDirectory(fileId: String): Boolean {
        val file = driveService.files()[fileId].execute()
        return file.mimeType == FOLDER_MIME_TYPE
    }

    // 共用方法: 獲取文件信息
    private fun getFileInfo(fileId: String, fields: String = "id,name,mimeType"): File {
        return driveService.files().get(fileId).setFields(fields).execute()
    }

    // 共用方法: 通用重命名功能
    private fun rename(fileId: String, newName: String): File {
        val fileMetadata = File().setName(newName)
        return driveService.files().update(fileId, fileMetadata).execute()
    }

    @Tool(description = "Create a new file in Google Drive, optionally in a specific directory")
    fun createNewFile(
        @ToolArg(description = "File name") fileName: String,
        @ToolArg(description = "File content, can be empty") content: String = "",
        @ToolArg(description = "File type, such as: text/plain") mimeType: String = "text/plain",
        @ToolArg(description = "Parent directory ID, leave empty for root directory") parentId: String? = null
    ): String {
        // 設置文件元數據
        val fileMetadata = File().setName(fileName)

        // 如果提供了 parentId，則將其設為父目錄
        if (!parentId.isNullOrEmpty()) {
            fileMetadata.parents = listOf(parentId)
        }
        // 將內容轉換為字節數組
        val contentBytes = content.toByteArray(StandardCharsets.UTF_8)
        val mediaContent = com.google.api.client.http.ByteArrayContent(mimeType, contentBytes)

        return try {
            // 上傳文件到 Google Drive
            val uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id, name, parents")
                .execute()

            createSuccessResponse(
                mapOf(
                    "fileId" to uploadedFile.id,
                    "fileName" to uploadedFile.name,
                    "parentId" to (uploadedFile.parents?.firstOrNull() ?: "root")
                )
            )
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Failed to create file")
        }
    }

    // 上傳文件
    @Tool(description = "Upload a file to Google Drive")
    fun uploadFile(
        @ToolArg(description = "Local file path") filePath: String,
        @ToolArg(description = "File name in Google Drive") fileName: String,
        @ToolArg(description = "File type, such as: text/plain") fileType: String
    ): String {
        return try {
            val fileMetadata = File().setName(fileName)
            val file = java.io.File(filePath)
            val mediaContent = FileContent(fileType, file)

            val uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id, name")
                .execute()

            createSuccessResponse(
                mapOf(
                    "fileId" to uploadedFile.id,
                    "fileName" to uploadedFile.name
                )
            )
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Failed to upload file")
        }
    }

    // 刪除文件
    @Tool(description = "Delete a file from Google Drive")
    fun deleteFile(@ToolArg(description = "Google Drive file ID") fileId: String): String {
        return try {
            driveService.files().delete(fileId).execute()
            createSuccessResponse(
                mapOf(
                    "message" to "File successfully deleted",
                    "fileId" to fileId
                )
            )
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Failed to delete file", mapOf("fileId" to fileId))
        }
    }

    // 讀取文件內容
    @Tool(description = "Read file content from Google Drive")
    fun readFile(@ToolArg(description = "Google Drive file ID") fileId: String): String {
        return try {
            val fileInfo = getFileInfo(fileId, "id,name,mimeType")
            val inputStream = driveService.files()[fileId].executeMediaAsInputStream()
            // 將文件內容轉換為字符串
            val content = if (fileInfo.mimeType.contains("text/") || fileInfo.mimeType.contains("application/json")) {
                IOUtils.toString(inputStream, StandardCharsets.UTF_8)
            } else {
                // 二進制文件返回 Base64 編碼
                "[Binary file - Please use download function]"
            }

            createSuccessResponse(
                mapOf(
                    "fileId" to fileId,
                    "fileName" to fileInfo.name,
                    "mimeType" to fileInfo.mimeType,
                    "content" to content
                )
            )
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Failed to read file", mapOf("fileId" to fileId))
        }
    }

    // 更新文件內容
    @Tool(description = "Update file content in Google Drive")
    fun updateFile(
        @ToolArg(description = "Google Drive file ID") fileId: String,
        @ToolArg(description = "New file content as string") content: String,
        @ToolArg(description = "File type, such as: text/plain") fileType: String = "text/plain"
    ): String {
        return try {
            val fileMetadata = File()
            // 將內容轉換為字節數組
            val contentBytes = content.toByteArray(StandardCharsets.UTF_8)
            val mediaContent = com.google.api.client.http.ByteArrayContent(fileType, contentBytes)

            val updatedFile = driveService.files().update(fileId, fileMetadata, mediaContent)
                .setFields("id,name")
                .execute()

            createSuccessResponse(
                mapOf(
                    "message" to "File content updated successfully",
                    "fileId" to updatedFile.id,
                    "fileName" to updatedFile.name
                )
            )
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Failed to update file", mapOf("fileId" to fileId))
        }
    }

    // 使用本地文件更新文件內容
    @Tool(description = "Update file content in Google Drive using local file")
    fun updateFileFromLocal(
        @ToolArg(description = "Google Drive file ID") fileId: String,
        @ToolArg(description = "Local file path to upload") filePath: String,
        @ToolArg(description = "File type, such as: text/plain") fileType: String
    ): String {
        return try {
            val fileMetadata = File()
            val file = java.io.File(filePath)
            val mediaContent = FileContent(fileType, file)

            val updatedFile = driveService.files().update(fileId, fileMetadata, mediaContent)
                .setFields("id,name")
                .execute()

            createSuccessResponse(
                mapOf(
                    "message" to "File content updated successfully from local file",
                    "fileId" to updatedFile.id,
                    "fileName" to updatedFile.name
                )
            )
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Failed to update file", mapOf("fileId" to fileId))
        }
    }

    // 列出所有文件
    @Tool(description = "List all files in Google Drive")
    fun listFiles(): String {
        return try {
            val result = driveService.files().list()
                .setPageSize(10)
                .setQ("trashed = false")  // 添加此條件排除垃圾桶檔案
                .setFields("nextPageToken, files(id, name, mimeType)")
                .execute()


            val files = result.files.map { file ->
                mapOf(
                    "id" to file.id,
                    "name" to file.name,
                    "type" to if (file.mimeType == FOLDER_MIME_TYPE) "Directory" else "File"
                )
            }

            createSuccessResponse(
                mapOf(
                    "files" to files,
                    "totalCount" to files.size
                )
            )
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Failed to list files")
        }
    }

    // 通過文件名獲取文件 ID
    @Tool(description = "Get file ID by file name in Google Drive")
    fun getFileIdByName(@ToolArg(description = "File name in Google Drive") fileName: String): String {
        return try {
            val result = driveService.files().list()
                .setQ("name = '$fileName' and trashed = false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()

            val files = result.files
            if (!files.isNullOrEmpty()) {
                createSuccessResponse(
                    mapOf(
                        "fileId" to files[0].id,
                        "fileName" to files[0].name
                    )
                )
            } else {
                createErrorResponse("Could not find file named '$fileName'")
            }
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Failed to get file ID")
        }
    }

    // 高級搜索功能，支持模糊匹配
    @Tool(description = "Search files in Google Drive and return file ID list")
    fun searchFiles(@ToolArg(description = "Search keyword") query: String): String {
        return try {
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

            createSuccessResponse(
                mapOf(
                    "files" to files,
                    "totalCount" to files.size,
                    "query" to query
                )
            )
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Failed to search files", mapOf("query" to query))
        }
    }

    // 通過文件 ID 獲取文件
    @Tool(description = "Get file details by file ID")
    fun getFileById(@ToolArg(description = "Google Drive file ID") fileId: String): String {
        return try {
            val file = driveService.files().get(fileId)
                .setFields("id, name, mimeType, size, createdTime, modifiedTime, parents")
                .execute()

            createSuccessResponse(
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
            )
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Failed to get file", mapOf("fileId" to fileId))
        }
    }

    // 重命名文件
    @Tool(description = "Change file name in Google Drive")
    fun renameFile(
        @ToolArg(description = "Google Drive file ID") fileId: String,
        @ToolArg(description = "New name") newName: String
    ): String {
        return try {
            val renamedFile = rename(fileId, newName)
            createSuccessResponse(
                mapOf(
                    "message" to "File renamed successfully",
                    "fileId" to fileId,
                    "newName" to newName,
                    "oldName" to renamedFile.name
                )
            )
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Failed to rename", mapOf("fileId" to fileId))
        }
    }

    // 在根目錄創建目錄
    @Tool(description = "Create directory in Google Drive root")
    fun createDirectory(
        @ToolArg(description = "Directory name") directoryName: String
    ): String {
        return try {
            val fileMetadata = File().setName(directoryName).setMimeType(FOLDER_MIME_TYPE)
            val createdDir = driveService.files().create(fileMetadata)
                .setFields("id, name")
                .execute()

            createSuccessResponse(
                mapOf(
                    "message" to "Directory created successfully",
                    "directoryId" to createdDir.id,
                    "directoryName" to createdDir.name
                )
            )
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Failed to create directory")
        }
    }

    // 在指定父目錄下創建目錄
    @Tool(description = "Create directory in a specified parent directory in Google Drive")
    fun createDirectoryInParent(
        @ToolArg(description = "Directory name") directoryName: String,
        @ToolArg(description = "Parent directory ID") parentId: String
    ): String {
        return try {
            val fileMetadata = File().setName(directoryName).setMimeType(FOLDER_MIME_TYPE)
            fileMetadata.parents = listOf(parentId)

            val createdDir = driveService.files().create(fileMetadata)
                .setFields("id, name")
                .execute()

            createSuccessResponse(
                mapOf(
                    "message" to "Subdirectory created successfully",
                    "directoryId" to createdDir.id,
                    "directoryName" to createdDir.name,
                    "parentId" to parentId
                )
            )
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Failed to create subdirectory", mapOf("parentId" to parentId))
        }
    }

    // 刪除目錄
    @Tool(description = "Delete a directory from Google Drive")
    fun deleteDirectory(
        @ToolArg(description = "Google Drive directory ID") directoryId: String
    ): String {
        return try {
            if (!isDirectory(directoryId)) {
                return createErrorResponse(
                    "ID $directoryId is not a directory, cannot execute directory deletion",
                    mapOf("directoryId" to directoryId)
                )
            }

            driveService.files().delete(directoryId).execute()
            createSuccessResponse(
                mapOf(
                    "message" to "Directory deleted successfully",
                    "directoryId" to directoryId
                )
            )
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Failed to delete directory", mapOf("directoryId" to directoryId))
        }
    }

    // 重命名目錄
    @Tool(description = "Rename a directory in Google Drive")
    fun renameDirectory(
        @ToolArg(description = "Google Drive directory ID") directoryId: String,
        @ToolArg(description = "New directory name") newName: String
    ): String {
        return try {
            if (!isDirectory(directoryId)) {
                return createErrorResponse(
                    "ID $directoryId is not a directory, cannot execute directory rename operation",
                    mapOf("directoryId" to directoryId)
                )
            }

            val renamedDir = rename(directoryId, newName)
            createSuccessResponse(
                mapOf(
                    "message" to "Directory renamed successfully",
                    "directoryId" to directoryId,
                    "newName" to newName,
                    "oldName" to renamedDir.name
                )
            )
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Failed to rename", mapOf("directoryId" to directoryId))
        }
    }

    // 列出目錄中的文件和子目錄
    @Tool(description = "List files and subdirectories in a Google Drive directory")
    fun listDirectoryContents(
        @ToolArg(description = "Google Drive directory ID, empty value means root directory") directoryId: String? = null
    ): String {
        return try {
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

            createSuccessResponse(
                mapOf(
                    "directoryId" to (directoryId ?: "root"),
                    "contents" to contents,
                    "totalCount" to contents.size
                )
            )
        } catch (e: Exception) {
            createErrorResponse(
                e.message ?: "Failed to list directory contents",
                mapOf("directoryId" to (directoryId ?: "root"))
            )
        }
    }

    // 將文件移動到指定目錄
    @Tool(description = "Move a file to a specified directory")
    fun moveFile(
        @ToolArg(description = "Google Drive file ID") fileId: String,
        @ToolArg(description = "Target directory ID") targetDirId: String
    ): String {
        return try {
            // 首先獲取文件的當前父目錄
            val file = getFileInfo(fileId, "id,name,parents")

            // 從所有父目錄中移除並添加到新目錄
            val previousParents = file.parents.joinToString(",")
            val updatedFile = driveService.files().update(fileId, null)
                .setAddParents(targetDirId)
                .setRemoveParents(previousParents)
                .setFields("id, parents, name")
                .execute()

            createSuccessResponse(
                mapOf(
                    "message" to "File moved successfully",
                    "fileId" to fileId,
                    "fileName" to updatedFile.name,
                    "targetDirectoryId" to targetDirId,
                    "previousParents" to previousParents
                )
            )
        } catch (e: Exception) {
            createErrorResponse(
                e.message ?: "Failed to move file", mapOf(
                    "fileId" to fileId,
                    "targetDirectoryId" to targetDirId
                )
            )
        }
    }

    // 檢查指定名稱的文件是否存在於目錄中
    @Tool(description = "Check if a file with specified name exists in a directory")
    fun checkFileExistsInDirectory(
        @ToolArg(description = "Directory ID") directoryId: String,
        @ToolArg(description = "File name to check") fileName: String
    ): String {
        return try {
            // 構建查詢：在指定目錄中查找有特定名稱且不在垃圾桶中的文件
            val query = "'$directoryId' in parents and name = '$fileName' and trashed = false"

            val result = driveService.files().list()
                .setQ(query)
                .setFields("files(id, name)")
                .execute()

            val files = result.files
            val exists = files != null && files.isNotEmpty()

            createSuccessResponse(
                mapOf(
                    "exists" to exists,
                    "directoryId" to directoryId,
                    "fileName" to fileName,
                    "fileId" to if (exists) files[0].id else null
                )
            )
        } catch (e: Exception) {
            createErrorResponse(
                e.message ?: "Error checking if file exists",
                mapOf(
                    "directoryId" to directoryId,
                    "fileName" to fileName
                )
            )
        }
    }

    // 搜索目錄功能
    @Tool(description = "Search directories in Google Drive by name")
    fun searchDirectories(
        @ToolArg(description = "Directory name or keyword to search") query: String,
        @ToolArg(description = "Whether to perform exact match (true) or partial match (false)") exactMatch: Boolean = false
    ): String {
        return try {
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

            createSuccessResponse(
                mapOf(
                    "directories" to directories,
                    "totalCount" to directories.size,
                    "query" to query,
                    "exactMatch" to exactMatch
                )
            )
        } catch (e: Exception) {
            createErrorResponse(
                e.message ?: "Failed to search directories",
                mapOf(
                    "query" to query,
                    "exactMatch" to exactMatch
                )
            )
        }
    }
}
