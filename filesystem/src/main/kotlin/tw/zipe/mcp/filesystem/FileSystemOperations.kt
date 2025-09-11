package tw.zipe.mcp.filesystem

import com.google.gson.Gson
import io.quarkiverse.mcp.server.Tool
import io.quarkiverse.mcp.server.ToolArg
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * File System Operations Utility Class
 * @author Gary
 * @created 2025/4/20
 */
class FileSystemOperations {

    private val fileServerPaths = System.getProperty("fileserver.paths")
        .split(",")
        .map { File(it).absolutePath }
        .filter { it.isNotBlank() }

    private companion object {
        private val gson = Gson()
    }

    // Check if path is in allowed directories
    private fun isPathAllowed(path: String): Boolean {
        if (fileServerPaths.isEmpty()) {
            return false
        }

        val filePath = File(path).absolutePath
        return fileServerPaths.any { allowedPath ->
            filePath == allowedPath || // Exact match
                    filePath.startsWith(allowedPath + File.separator) // Subdirectory match
        }
    }

    // Generate JSON success response
    private fun createSuccessResponse(data: Map<String, Any?>): String {
        val responseMap = mutableMapOf<String, Any?>("success" to true)
        responseMap.putAll(data)
        return gson.toJson(responseMap)
    }

    // Generate JSON error response
    private fun createErrorResponse(error: String, data: Map<String, Any?> = emptyMap()): String {
        val responseMap = mutableMapOf<String, Any?>("success" to false, "error" to error)
        responseMap.putAll(data)
        return gson.toJson(responseMap)
    }

    // Execute file operation and handle exceptions
    private inline fun <T> executeFileOperation(
        path: String,
        operation: () -> T
    ): String {
        return runCatching {
            if (!isPathAllowed(path)) {
                return createErrorResponse("Access to this path is not allowed")
            }
            operation()
        }.fold(
            onSuccess = { it as String },
            onFailure = { createErrorResponse(it.message ?: "Operation failed") }
        )
    }

    // Check file existence and execute operation
    private inline fun executeIfFileExists(
        filePath: String,
        requireFile: Boolean = false,
        crossinline operation: (File) -> String
    ): String {
        return executeFileOperation(filePath) {
            val file = File(filePath)
            if (!file.exists()) {
                return@executeFileOperation createErrorResponse("File or directory does not exist")
            }
            if (requireFile && !file.isFile) {
                return@executeFileOperation createErrorResponse("Specified path is not a file")
            }
            operation(file)
        }
    }

    // Check if file exists
    @Tool(description = "Check if file exists")
    fun checkFileExists(
        @ToolArg(description = "File path") filePath: String
    ): String {
        return executeFileOperation(filePath) {
            val file = File(filePath)
            val exists = file.exists()

            createSuccessResponse(
                mapOf(
                    "filePath" to filePath,
                    "exists" to exists,
                    "isFile" to (exists && file.isFile),
                    "isDirectory" to (exists && file.isDirectory)
                )
            )
        }
    }

    // Create new file
    @Tool(description = "Create new file")
    fun createFile(
        @ToolArg(description = "File path") filePath: String,
        @ToolArg(description = "File content") content: String,
        @ToolArg(description = "Character encoding, default UTF-8") charset: String = "UTF-8"
    ): String {
        return executeFileOperation(filePath) {
            val file = File(filePath)

            if (file.exists()) {
                return@executeFileOperation createErrorResponse("File already exists")
            }

            // Ensure parent directory exists
            file.parentFile?.mkdirs()

            // Write file content
            val charsetObj = Charset.forName(charset)
            Files.write(Paths.get(filePath), content.toByteArray(charsetObj))

            createSuccessResponse(
                mapOf(
                    "filePath" to filePath,
                    "created" to true,
                    "size" to file.length(),
                    "charset" to charset
                )
            )
        }
    }

    // Read file content
    @Tool(description = "Read file content")
    fun readFile(
        @ToolArg(description = "File path") filePath: String,
        @ToolArg(description = "Character encoding, default UTF-8") charset: String = "UTF-8"
    ): String {
        return executeIfFileExists(filePath, requireFile = true) { file ->
            val content = String(Files.readAllBytes(file.toPath()), Charset.forName(charset))

            createSuccessResponse(
                mapOf(
                    "filePath" to filePath,
                    "content" to content,
                    "size" to file.length(),
                    "charset" to charset
                )
            )
        }
    }

    // Update file content
    @Tool(description = "Update file content")
    fun updateFile(
        @ToolArg(description = "File path") filePath: String,
        @ToolArg(description = "New file content") content: String,
        @ToolArg(description = "Character encoding, default UTF-8") charset: String = "UTF-8"
    ): String {
        return executeIfFileExists(filePath, requireFile = true) { file ->
            val oldSize = file.length()
            Files.write(file.toPath(), content.toByteArray(Charset.forName(charset)))

            createSuccessResponse(
                mapOf(
                    "filePath" to filePath,
                    "updated" to true,
                    "oldSize" to oldSize,
                    "newSize" to file.length(),
                    "charset" to charset
                )
            )
        }
    }

    // Delete file or directory
    @Tool(description = "Delete file or directory")
    fun deleteFile(
        @ToolArg(description = "File or directory path") path: String,
        @ToolArg(description = "Whether to recursively delete, applicable to directories") recursive: String = "false"
    ): String {
        return executeIfFileExists(path) { file ->
            val isFile = file.isFile
            val recursiveDelete = recursive.toBoolean()
            val isDeleted = if (isFile) {
                file.delete()
            } else if (recursiveDelete) {
                file.deleteRecursively()
            } else {
                file.delete()
            }

            createSuccessResponse(
                mapOf(
                    "path" to path,
                    "deleted" to isDeleted,
                    "wasFile" to isFile,
                    "wasDirectory" to !isFile
                )
            )
        }
    }

    // Copy file
    @Tool(description = "Copy file")
    fun copyFile(
        @ToolArg(description = "Source file path") sourcePath: String,
        @ToolArg(description = "Target file path") targetPath: String,
        @ToolArg(description = "Whether to overwrite if target file exists") replace: String = "false"
    ): String {
        return executeFileOperation(sourcePath) {
            if (!isPathAllowed(targetPath)) {
                return@executeFileOperation createErrorResponse("Access to target path is not allowed")
            }

            val sourceFile = File(sourcePath)
            val targetFile = File(targetPath)

            if (!sourceFile.exists()) {
                return@executeFileOperation createErrorResponse("Source file does not exist")
            }

            if (!sourceFile.isFile) {
                return@executeFileOperation createErrorResponse("Source path is not a file")
            }

            val replaceBoolean = replace.toBoolean()
            if (targetFile.exists() && !replaceBoolean) {
                return@executeFileOperation createErrorResponse("Target file already exists and overwrite not specified")
            }

            // Ensure target directory exists
            targetFile.parentFile?.mkdirs()

            val copyOptions = if (replaceBoolean) {
                arrayOf(StandardCopyOption.REPLACE_EXISTING)
            } else {
                emptyArray()
            }

            Files.copy(sourceFile.toPath(), targetFile.toPath(), *copyOptions)

            createSuccessResponse(
                mapOf(
                    "sourcePath" to sourcePath,
                    "targetPath" to targetPath,
                    "copied" to true,
                    "size" to targetFile.length(),
                    "replaced" to replaceBoolean
                )
            )
        }
    }

    // Move file
    @Tool(description = "Move file")
    fun moveFile(
        @ToolArg(description = "Source file path") sourcePath: String,
        @ToolArg(description = "Target file path") targetPath: String,
        @ToolArg(description = "Whether to overwrite if target file exists") replace: String = "false"
    ): String {
        return executeFileOperation(sourcePath) {
            if (!isPathAllowed(targetPath)) {
                return@executeFileOperation createErrorResponse("Access to target path is not allowed")
            }

            val sourceFile = File(sourcePath)
            val targetFile = File(targetPath)

            if (!sourceFile.exists()) {
                return@executeFileOperation createErrorResponse("Source file does not exist")
            }

            val replaceBoolean = replace.toBoolean()
            if (targetFile.exists() && !replaceBoolean) {
                return@executeFileOperation createErrorResponse("Target file already exists and overwrite not specified")
            }

            // Ensure target directory exists
            targetFile.parentFile?.mkdirs()

            val moveOptions = if (replaceBoolean) {
                arrayOf(StandardCopyOption.REPLACE_EXISTING)
            } else {
                emptyArray()
            }

            Files.move(sourceFile.toPath(), targetFile.toPath(), *moveOptions)

            createSuccessResponse(
                mapOf(
                    "sourcePath" to sourcePath,
                    "targetPath" to targetPath,
                    "moved" to true,
                    "replaced" to replaceBoolean
                )
            )
        }
    }

    // List directory contents
    @Tool(description = "List directory contents")
    fun listDirectory(
        @ToolArg(description = "Directory path") directoryPath: String,
        @ToolArg(description = "Whether to list only files") filesOnly: String = "false",
        @ToolArg(description = "Whether to list only directories") directoriesOnly: String = "false"
    ): String {
        return executeFileOperation(directoryPath) {
            val directory = File(directoryPath)

            if (!directory.exists()) {
                return@executeFileOperation createErrorResponse("Directory does not exist")
            }

            if (!directory.isDirectory) {
                return@executeFileOperation createErrorResponse("Specified path is not a directory")
            }

            val filesOnlyBoolean = filesOnly.toBoolean()
            val directoriesOnlyBoolean = directoriesOnly.toBoolean()

            val files = directory.listFiles() ?: emptyArray()
            val filteredFiles = when {
                filesOnlyBoolean -> files.filter { it.isFile }
                directoriesOnlyBoolean -> files.filter { it.isDirectory }
                else -> files.toList()
            }

            val fileInfos = filteredFiles.map { file ->
                mapOf(
                    "name" to file.name,
                    "path" to file.absolutePath,
                    "isFile" to file.isFile,
                    "isDirectory" to file.isDirectory,
                    "size" to if (file.isFile) file.length() else null,
                    "lastModified" to file.lastModified()
                )
            }

            createSuccessResponse(
                mapOf(
                    "directoryPath" to directoryPath,
                    "contents" to fileInfos,
                    "count" to fileInfos.size,
                    "filesOnly" to filesOnlyBoolean,
                    "directoriesOnly" to directoriesOnlyBoolean
                )
            )
        }
    }

    // Get file info
    @Tool(description = "Get detailed information about a file or directory")
    fun getFileInfo(
        @ToolArg(description = "File or directory path") path: String
    ): String {
        return executeIfFileExists(path) { file ->
            val info = mapOf(
                "path" to file.absolutePath,
                "name" to file.name,
                "exists" to file.exists(),
                "isFile" to file.isFile,
                "isDirectory" to file.isDirectory,
                "isHidden" to file.isHidden,
                "size" to if (file.isFile) file.length() else null,
                "lastModified" to file.lastModified(),
                "canRead" to file.canRead(),
                "canWrite" to file.canWrite(),
                "canExecute" to file.canExecute(),
                "parent" to file.parent
            )

            createSuccessResponse(info)
        }
    }

    // Create new directory
    @Tool(description = "Create new directory")
    fun createDirectory(
        @ToolArg(description = "Directory path") directoryPath: String,
        @ToolArg(description = "Whether to create parent directories if they don't exist") createParents: String = "true"
    ): String {
        return executeFileOperation(directoryPath) {
            val directory = File(directoryPath)

            if (directory.exists()) {
                if (directory.isDirectory) {
                    return@executeFileOperation createSuccessResponse(
                        mapOf(
                            "directoryPath" to directoryPath,
                            "created" to false,
                            "alreadyExists" to true
                        )
                    )
                } else {
                    return@executeFileOperation createErrorResponse("Specified path exists but is not a directory")
                }
            }

            val success = if (createParents.toBoolean()) {
                directory.mkdirs()
            } else {
                directory.mkdir()
            }

            if (!success) {
                return@executeFileOperation createErrorResponse("Unable to create directory")
            }

            createSuccessResponse(
                mapOf(
                    "directoryPath" to directoryPath,
                    "created" to true,
                    "withParents" to createParents.toBoolean()
                )
            )
        }
    }
}
