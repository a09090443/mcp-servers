package tw.zipe.mcp.filesystem

import java.io.File
import java.nio.file.Files
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * @author zipe1
 * @created 2025/4/21
 */
class FileSystemOperationsTest {

    private lateinit var fileSystemOperations: FileSystemOperations

    @TempDir
    lateinit var tempDir: File

    private lateinit var testFilePath: String
    private lateinit var testDirPath: String

    companion object {
        @BeforeAll
        @JvmStatic
        fun setupAll() {
            System.setProperty("fileserver.paths", System.getProperty("java.io.tmpdir"))
        }

        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            System.clearProperty("fileserver.paths")
            unmockkAll()
        }
    }

    @BeforeEach
    fun setup() {
        fileSystemOperations = FileSystemOperations()
        testFilePath = File(tempDir, "test.txt").absolutePath
        testDirPath = File(tempDir, "testDir").absolutePath
        File(testDirPath).mkdir()
    }

    @AfterEach
    fun tearDown() {
        File(testFilePath).delete()
        File(testDirPath).deleteRecursively()
    }

    @Test
    fun `test checkFileExists when file does not exist`() {
        val response = fileSystemOperations.checkFileExists(testFilePath)

        assertTrue(response.contains("\"success\":true"))
        assertTrue(response.contains("\"exists\":false"))
    }

    @Test
    fun `test checkFileExists when file exists`() {
        Files.write(File(testFilePath).toPath(), "test content".toByteArray())

        val response = fileSystemOperations.checkFileExists(testFilePath)

        assertTrue(response.contains("\"success\":true"))
        assertTrue(response.contains("\"exists\":true"))
        assertTrue(response.contains("\"isFile\":true"))
        assertTrue(response.contains("\"isDirectory\":false"))
    }

    @Test
    fun `test createFile successfully`() {
        val content = "Hello, World!"
        val response = fileSystemOperations.createFile(testFilePath, content)

        assertTrue(response.contains("\"success\":true"))
        assertTrue(response.contains("\"created\":true"))

        val fileContent = Files.readString(File(testFilePath).toPath())
        assertEquals(content, fileContent)
    }

    @Test
    fun `test createFile when file already exists`() {
        Files.write(File(testFilePath).toPath(), "original content".toByteArray())

        val response = fileSystemOperations.createFile(testFilePath, "new content")

        assertTrue(response.contains("\"success\":false"))
        assertTrue(response.contains("File already exists"))

        val fileContent = Files.readString(File(testFilePath).toPath())
        assertEquals("original content", fileContent)
    }

    @Test
    fun `test readFile successfully`() {
        val content = "Test content for reading"
        Files.write(File(testFilePath).toPath(), content.toByteArray())

        val response = fileSystemOperations.readFile(testFilePath)

        assertTrue(response.contains("\"success\":true"))
        assertTrue(response.contains("\"content\":\"$content\""))
    }

    @Test
    fun `test readFile when file does not exist`() {
        val response = fileSystemOperations.readFile(testFilePath)

        assertTrue(response.contains("\"success\":false"))
        assertTrue(response.contains("does not exist"))
    }

    @Test
    fun `test updateFile successfully`() {
        val initialContent = "Initial content"
        Files.write(File(testFilePath).toPath(), initialContent.toByteArray())

        val newContent = "Updated content"
        val response = fileSystemOperations.updateFile(testFilePath, newContent)

        assertTrue(response.contains("\"success\":true"))
        assertTrue(response.contains("\"updated\":true"))

        val fileContent = Files.readString(File(testFilePath).toPath())
        assertEquals(newContent, fileContent)
    }

    @Test
    fun `test updateFile when file does not exist`() {
        val response = fileSystemOperations.updateFile(testFilePath, "Updated content")

        assertTrue(response.contains("\"success\":false"))
        assertTrue(response.contains("does not exist"))
    }

    @Test
    fun `test deleteFile successfully`() {
        Files.write(File(testFilePath).toPath(), "content to delete".toByteArray())
        assertTrue(File(testFilePath).exists())

        val response = fileSystemOperations.deleteFile(testFilePath)

        assertTrue(response.contains("\"success\":true"))
        assertTrue(response.contains("\"deleted\":true"))
        assertFalse(File(testFilePath).exists())
    }

    @Test
    fun `test deleteFile when file does not exist`() {
        val response = fileSystemOperations.deleteFile(testFilePath)

        assertTrue(response.contains("\"success\":false"))
        assertTrue(response.contains("does not exist"))
    }

    @Test
    fun `test deleteFile recursively on directory`() {
        val nestedFile = File(testDirPath, "nested.txt")
        Files.write(nestedFile.toPath(), "nested content".toByteArray())

        val response = fileSystemOperations.deleteFile(testDirPath, true)

        assertTrue(response.contains("\"success\":true"))
        assertTrue(response.contains("\"deleted\":true"))
        assertFalse(File(testDirPath).exists())
    }

    @Test
    fun `test copyFile successfully`() {
        val sourceContent = "Content to copy"
        Files.write(File(testFilePath).toPath(), sourceContent.toByteArray())

        val targetPath = File(tempDir, "target.txt").absolutePath
        val response = fileSystemOperations.copyFile(testFilePath, targetPath)

        assertTrue(response.contains("\"success\":true"))
        assertTrue(response.contains("\"copied\":true"))

        val targetContent = Files.readString(File(targetPath).toPath())
        assertEquals(sourceContent, targetContent)
    }

    @Test
    fun `test copyFile when source does not exist`() {
        val targetPath = File(tempDir, "target.txt").absolutePath
        val response = fileSystemOperations.copyFile(testFilePath, targetPath)

        assertTrue(response.contains("\"success\":false"))
        assertTrue(response.contains("Source file does not exist"))
    }

    @Test
    fun `test copyFile with replace option`() {
        // 建立來源檔案
        val sourceContent = "Original source content"
        Files.write(File(testFilePath).toPath(), sourceContent.toByteArray())

        // 建立目標檔案（已存在）
        val targetPath = File(tempDir, "target.txt").absolutePath
        Files.write(File(targetPath).toPath(), "Original target content".toByteArray())

        // 使用replace=true複製檔案
        val response = fileSystemOperations.copyFile(testFilePath, targetPath, true)

        assertTrue(response.contains("\"success\":true"))
        assertTrue(response.contains("\"copied\":true"))
        assertTrue(response.contains("\"replaced\":true"))

        // 驗證目標檔案內容已被覆寫
        val targetContent = Files.readString(File(targetPath).toPath())
        assertEquals(sourceContent, targetContent)
    }

    @Test
    fun `test copyFile without replace option when target exists`() {
        // 建立來源檔案
        Files.write(File(testFilePath).toPath(), "Source content".toByteArray())

        // 建立目標檔案（已存在）
        val targetPath = File(tempDir, "target.txt").absolutePath
        val originalTargetContent = "Original target content"
        Files.write(File(targetPath).toPath(), originalTargetContent.toByteArray())

        // 嘗試複製但不覆寫
        val response = fileSystemOperations.copyFile(testFilePath, targetPath, false)

        assertTrue(response.contains("\"success\":false"))
        assertTrue(response.contains("Target file already exists"))

        // 驗證目標檔案內容未被修改
        val targetContent = Files.readString(File(targetPath).toPath())
        assertEquals(originalTargetContent, targetContent)
    }

    @Test
    fun `test moveFile successfully`() {
        val sourceContent = "Content to move"
        Files.write(File(testFilePath).toPath(), sourceContent.toByteArray())

        val targetPath = File(tempDir, "moved.txt").absolutePath
        val response = fileSystemOperations.moveFile(testFilePath, targetPath)

        assertTrue(response.contains("\"success\":true"))
        assertTrue(response.contains("\"moved\":true"))

        assertFalse(File(testFilePath).exists())
        assertTrue(File(targetPath).exists())

        val targetContent = Files.readString(File(targetPath).toPath())
        assertEquals(sourceContent, targetContent)
    }

    @Test
    fun `test moveFile with replace option`() {
        // 建立來源檔案
        val sourceContent = "Source content to move"
        Files.write(File(testFilePath).toPath(), sourceContent.toByteArray())

        // 建立目標檔案（已存在）
        val targetPath = File(tempDir, "target.txt").absolutePath
        Files.write(File(targetPath).toPath(), "Original target content".toByteArray())

        // 使用replace=true移動檔案
        val response = fileSystemOperations.moveFile(testFilePath, targetPath, true)

        assertTrue(response.contains("\"success\":true"))
        assertTrue(response.contains("\"moved\":true"))
        assertTrue(response.contains("\"replaced\":true"))

        // 驗證來源檔案已刪除
        assertFalse(File(testFilePath).exists())

        // 驗證目標檔案內容已被覆寫
        val targetContent = Files.readString(File(targetPath).toPath())
        assertEquals(sourceContent, targetContent)
    }

    @Test
    fun `test moveFile without replace option when target exists`() {
        // 建立來源檔案
        val sourceContent = "Source content to move"
        Files.write(File(testFilePath).toPath(), sourceContent.toByteArray())

        // 建立目標檔案（已存在）
        val targetPath = File(tempDir, "target.txt").absolutePath
        val originalContent = "Original target content"
        Files.write(File(targetPath).toPath(), originalContent.toByteArray())

        // 嘗試移動但不覆寫
        val response = fileSystemOperations.moveFile(testFilePath, targetPath, false)

        assertTrue(response.contains("\"success\":false"))
        assertTrue(response.contains("Target file already exists"))

        // 驗證來源檔案仍然存在
        assertTrue(File(testFilePath).exists())

        // 驗證目標檔案內容未被修改
        val targetContent = Files.readString(File(targetPath).toPath())
        assertEquals(originalContent, targetContent)
    }

    @Test
    fun `test moveFile when source does not exist`() {
        val targetPath = File(tempDir, "moved.txt").absolutePath
        val response = fileSystemOperations.moveFile(testFilePath, targetPath)

        assertTrue(response.contains("\"success\":false"))
        assertTrue(response.contains("Source file does not exist"))
    }

    @Test
    fun `test listDirectory successfully`() {
        // 建立測試檔案和目錄
        Files.write(File(testFilePath).toPath(), "test content".toByteArray())
        File(testDirPath).mkdir()

        val response = fileSystemOperations.listDirectory(tempDir.absolutePath)

        assertTrue(response.contains("\"success\":true"))
        assertTrue(response.contains("test.txt"))
        assertTrue(response.contains("testDir"))
    }

    @Test
    fun `test listDirectory with filesOnly filter`() {
        Files.write(File(testFilePath).toPath(), "test content".toByteArray())
        File(testDirPath).mkdir()

        val response = fileSystemOperations.listDirectory(tempDir.absolutePath, filesOnly = true)

        assertTrue(response.contains("\"success\":true"))
        assertTrue(response.contains("test.txt"))
        assertFalse(response.contains("\"name\":\"testDir\""))
    }

    @Test
    fun `test listDirectory with directoriesOnly filter`() {
        Files.write(File(testFilePath).toPath(), "test content".toByteArray())
        File(testDirPath).mkdir()

        val response = fileSystemOperations.listDirectory(tempDir.absolutePath, directoriesOnly = true)

        assertTrue(response.contains("\"success\":true"))
        assertTrue(response.contains("\"name\":\"testDir\""))
        assertFalse(response.contains("\"name\":\"test.txt\""))
    }

    @Test
    fun `test listDirectory when directory does not exist`() {
        val nonExistentDir = File(tempDir, "nonexistent").absolutePath
        val response = fileSystemOperations.listDirectory(nonExistentDir)

        assertTrue(response.contains("\"success\":false"))
        assertTrue(response.contains("Directory does not exist"))
    }

    @Test
    fun `test listDirectory on file path`() {
        Files.write(File(testFilePath).toPath(), "test content".toByteArray())

        val response = fileSystemOperations.listDirectory(testFilePath)

        assertTrue(response.contains("\"success\":false"))
        assertTrue(response.contains("not a directory"))
    }

    @Test
    fun `test getFileInfo for file`() {
        val content = "Test content for info"
        Files.write(File(testFilePath).toPath(), content.toByteArray())

        val response = fileSystemOperations.getFileInfo(testFilePath)

        assertTrue(response.contains("\"success\":true"))
        assertTrue(response.contains("\"isFile\":true"))
        assertTrue(response.contains("\"isDirectory\":false"))
        assertTrue(response.contains("\"name\":\"test.txt\""))
    }

    @Test
    fun `test getFileInfo for directory`() {
        val response = fileSystemOperations.getFileInfo(testDirPath)

        assertTrue(response.contains("\"success\":true"))
        assertTrue(response.contains("\"isFile\":false"))
        assertTrue(response.contains("\"isDirectory\":true"))
        assertTrue(response.contains("\"name\":\"testDir\""))
    }

    @Test
    fun `test getFileInfo when path does not exist`() {
        val nonExistentPath = File(tempDir, "nonexistent.txt").absolutePath
        val response = fileSystemOperations.getFileInfo(nonExistentPath)

        assertTrue(response.contains("\"success\":false"))
        assertTrue(response.contains("does not exist"))
    }

    @Test
    fun `test path access restriction`() {
        // 保存原始屬性值
        val originalProperty = System.getProperty("fileserver.paths")

        try {
            // 設置特定允許路徑
            System.setProperty("fileserver.paths", "/allowed/path")

            // 應該被限制的路徑
            val restrictedPath = "/restricted/path.txt"

            // 建立新實例，使用更新後的屬性
            val restrictedOperations = FileSystemOperations()

            // 測試限制
            val response = restrictedOperations.checkFileExists(restrictedPath)

            assertTrue(response.contains("\"success\":false"))
            assertTrue(response.contains("Access to this path is not allowed"))
        } finally {
            // 恢復原始屬性
            if (originalProperty != null) {
                System.setProperty("fileserver.paths", originalProperty)
            } else {
                System.clearProperty("fileserver.paths")
            }
        }
    }

    @Test
    fun `test creating file with custom charset`() {
        val content = "測試內容"
        val charset = "GBK"
        val response = fileSystemOperations.createFile(testFilePath, content, charset)

        assertTrue(response.contains("\"success\":true"))
        assertTrue(response.contains("\"charset\":\"GBK\""))

        // 驗證檔案內容使用指定字符集
        val fileContent =
            String(Files.readAllBytes(File(testFilePath).toPath()), java.nio.charset.Charset.forName(charset))
        assertEquals(content, fileContent)
    }

    @Test
    fun `test reading file with custom charset`() {
        val content = "測試內容"
        val charset = "UTF-8"

        // 使用UTF-8編碼寫入檔案
        Files.write(File(testFilePath).toPath(), content.toByteArray(java.nio.charset.Charset.forName(charset)))

        // 使用相同編碼讀取
        val response = fileSystemOperations.readFile(testFilePath, charset)

        assertTrue(response.contains("\"success\":true"))
        assertTrue(response.contains("\"charset\":\"UTF-8\""))
        assertTrue(response.contains("\"content\":\"$content\""))
    }
}
