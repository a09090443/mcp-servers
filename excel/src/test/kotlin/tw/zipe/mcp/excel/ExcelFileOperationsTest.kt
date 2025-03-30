package tw.zipe.mcp.excel

import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ExcelFileOperationsTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var excelOps: ExcelFileOperations
    private lateinit var testFilePath: String
    private lateinit var testCsvPath: String
    private val gson = Gson()

    @BeforeEach
    fun setup() {
        excelOps = ExcelFileOperations()
        testFilePath = tempDir.resolve("test.xlsx").toString()
        testCsvPath = tempDir.resolve("test.csv").toString()

        // 创建测试Excel文件
        val workbook = XSSFWorkbook()
        workbook.createSheet("TestSheet")
        FileOutputStream(testFilePath).use {
            workbook.write(it)
        }
        workbook.close()

        // 创建测试CSV文件
        Files.write(Paths.get(testCsvPath), listOf("A1,B1,C1", "A2,B2,C2", "A3,B3,C3"))
    }

    @AfterEach
    fun cleanup() {
        // 确保临时文件被删除
        File(testFilePath).delete()
        File(testCsvPath).delete()
    }

    @Test
    fun testCreateExcelFile() {
        val newFileName = tempDir.resolve("newTest.xlsx").toString()
        val response = excelOps.createExcelFile(newFileName, "NewSheet")
        val result = gson.fromJson(response, Map::class.java)

        assertTrue(result["success"] as Boolean)
        assertEquals("Excel file created successfully", result["message"])
        assertTrue(File(newFileName).exists())

        // 验证文件内容
        XSSFWorkbook(newFileName).use { workbook ->
            assertEquals(1, workbook.numberOfSheets)
            assertEquals("NewSheet", workbook.getSheetName(0))
        }
    }

    @Test
    fun testAddWorksheet() {
        val response = excelOps.addWorksheet(testFilePath, "NewSheet")
        val result = gson.fromJson(response, Map::class.java)

        assertTrue(result["success"] as Boolean)
        assertEquals("Worksheet added successfully", result["message"])

        // 验证工作表是否已添加
        XSSFWorkbook(testFilePath).use { workbook ->
            assertEquals(2, workbook.numberOfSheets)
            assertNotNull(workbook.getSheet("NewSheet"))
        }
    }

    @Test
    fun testAddWorksheetDuplicate() {
        // 尝试添加已存在的工作表
        val response = excelOps.addWorksheet(testFilePath, "TestSheet")
        val result = gson.fromJson(response, Map::class.java)

        assertFalse(result["success"] as Boolean)
        assertEquals("Worksheet 'TestSheet' already exists", result["error"])
    }

    @Test
    fun testDeleteWorksheet() {
        // 先添加一个新工作表，然后删除它
        excelOps.addWorksheet(testFilePath, "ToDelete")

        val response = excelOps.deleteWorksheet(testFilePath, "ToDelete")
        val result = gson.fromJson(response, Map::class.java)

        assertTrue(result["success"] as Boolean)
        assertEquals("Worksheet deleted successfully", result["message"])

        // 验证工作表是否已删除
        XSSFWorkbook(testFilePath).use { workbook ->
            assertEquals(1, workbook.numberOfSheets)
            assertNull(workbook.getSheet("ToDelete"))
        }
    }

    @Test
    fun testDeleteOnlyWorksheet() {
        // 尝试删除唯一的工作表
        val response = excelOps.deleteWorksheet(testFilePath, "TestSheet")
        val result = gson.fromJson(response, Map::class.java)

        assertFalse(result["success"] as Boolean)
        assertTrue((result["error"] as String).contains("Cannot delete the only worksheet"))
    }

    @Test
    fun testRenameWorksheet() {
        val response = excelOps.renameWorksheet(testFilePath, "TestSheet", "RenamedSheet")
        val result = gson.fromJson(response, Map::class.java)

        assertTrue(result["success"] as Boolean)
        assertEquals("Worksheet renamed successfully", result["message"])

        // 验证工作表是否已重命名
        XSSFWorkbook(testFilePath).use { workbook ->
            assertNotNull(workbook.getSheet("RenamedSheet"))
            assertNull(workbook.getSheet("TestSheet"))
        }
    }

    @Test
    fun testWriteAndReadCellData() {
        // 写入单元格数据
        val writeResponse = excelOps.writeCellData(testFilePath, "TestSheet", 1, 2, "TestData")
        val writeResult = gson.fromJson(writeResponse, Map::class.java)

        assertTrue(writeResult["success"] as Boolean)
        assertEquals("Cell data written successfully", writeResult["message"])

        // 读取并验证单元格数据
        val readResponse = excelOps.readCellData(testFilePath, "TestSheet", 1, 2)
        val readResult = gson.fromJson(readResponse, Map::class.java)

        assertTrue(readResult["success"] as Boolean)
        assertEquals("TestData", readResult["value"])
        assertEquals(true, readResult["exists"])
    }

    @Test
    fun testWriteAndReadRowData() {
        // 写入行数据
        val rowData = "A,B,C,D,E"
        val writeResponse = excelOps.writeRowData(testFilePath, "TestSheet", 3, rowData)
        val writeResult = gson.fromJson(writeResponse, Map::class.java)

        assertTrue(writeResult["success"] as Boolean)
        assertEquals("Row data written successfully", writeResult["message"])
        assertEquals(5, (writeResult["columnCount"] as Double).toInt())  // 将Double转为Int后比较

        // 读取并验证行数据
        val readResponse = excelOps.readRowData(testFilePath, "TestSheet", 3)
        val readResult = gson.fromJson(readResponse, Map::class.java)

        assertTrue(readResult["success"] as Boolean)
        val values = readResult["values"] as List<*>
        assertEquals(5, values.size)
        assertEquals("A", values[0])
        assertEquals("E", values[4])
    }

    @Test
    fun testListWorksheets() {
        // 先添加一些工作表
        excelOps.addWorksheet(testFilePath, "Sheet2")
        excelOps.addWorksheet(testFilePath, "Sheet3")

        val response = excelOps.listWorksheets(testFilePath)
        val result = gson.fromJson(response, Map::class.java)

        assertTrue(result["success"] as Boolean)
        val worksheets = result["worksheets"] as List<*>
        assertEquals(3, worksheets.size)
        assertTrue(worksheets.contains("TestSheet"))
        assertTrue(worksheets.contains("Sheet2"))
        assertTrue(worksheets.contains("Sheet3"))
    }

    @Test
    fun testImportCSV() {
        val response = excelOps.importCSV(testFilePath, testCsvPath, "CSVSheet")
        val result = gson.fromJson(response, Map::class.java)

        assertTrue(result["success"] as Boolean)
        assertEquals("CSV data imported successfully", result["message"])
        assertEquals(3, (result["rowCount"] as Double).toInt())  // 将Double转为Int后比较

        // 验证导入的数据
        XSSFWorkbook(testFilePath).use { workbook ->
            val sheet = workbook.getSheet("CSVSheet")
            assertNotNull(sheet)
            assertEquals(3, sheet.physicalNumberOfRows)
            assertEquals("A1", sheet.getRow(0).getCell(0).stringCellValue)
            assertEquals("B2", sheet.getRow(1).getCell(1).stringCellValue)
            assertEquals("C3", sheet.getRow(2).getCell(2).stringCellValue)
        }
    }

    @Test
    fun testExportToCSV() {
        // 先写入一些数据
        excelOps.writeRowData(testFilePath, "TestSheet", 0, "X1,Y1,Z1")
        excelOps.writeRowData(testFilePath, "TestSheet", 1, "X2,Y2,Z2")

        val exportPath = tempDir.resolve("export.csv").toString()
        val response = excelOps.exportToCSV(testFilePath, "TestSheet", exportPath)
        val result = gson.fromJson(response, Map::class.java)

        assertTrue(result["success"] as Boolean)
        assertEquals("Worksheet exported successfully", result["message"])

        // 验证导出的CSV文件
        val csvLines = Files.readAllLines(Paths.get(exportPath))
        assertEquals(2, csvLines.size)
        assertEquals("X1,Y1,Z1", csvLines[0])
        assertEquals("X2,Y2,Z2", csvLines[1])
    }

    @Test
    fun testMergeCells() {
        // 先写入一些数据
        excelOps.writeCellData(testFilePath, "TestSheet", 1, 1, "Merged Cell")

        val response = excelOps.mergeCells(testFilePath, "TestSheet", 1, 2, 1, 3)
        val result = gson.fromJson(response, Map::class.java)

        assertTrue(result["success"] as Boolean)
        assertEquals("Cells merged successfully", result["message"])

        // 验证单元格合并
        XSSFWorkbook(testFilePath).use { workbook ->
            val sheet = workbook.getSheet("TestSheet")
            assertEquals(1, sheet.numMergedRegions)
            val mergedRegion = sheet.getMergedRegion(0)
            assertEquals(1, mergedRegion.firstRow)
            assertEquals(2, mergedRegion.lastRow)
            assertEquals(1, mergedRegion.firstColumn)
            assertEquals(3, mergedRegion.lastColumn)
        }
    }

    @Test
    fun testFileNotExistError() {
        val nonExistentPath = tempDir.resolve("nonexistent.xlsx").toString()
        val response = excelOps.readCellData(nonExistentPath, "Sheet1", 0, 0)
        val result = gson.fromJson(response, Map::class.java)

        assertFalse(result["success"] as Boolean)
        assertEquals("File does not exist: $nonExistentPath", result["error"])
    }

    @Test
    fun testWorksheetNotExistError() {
        val response = excelOps.readCellData(testFilePath, "NonExistentSheet", 0, 0)
        val result = gson.fromJson(response, Map::class.java)

        assertFalse(result["success"] as Boolean)
        assertEquals("Worksheet 'NonExistentSheet' does not exist", result["error"])
    }
}
