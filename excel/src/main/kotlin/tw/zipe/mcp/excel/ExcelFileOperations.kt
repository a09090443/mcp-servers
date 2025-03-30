package tw.zipe.mcp.excel

        import com.google.gson.Gson
        import io.quarkiverse.mcp.server.Tool
        import io.quarkiverse.mcp.server.ToolArg
        import java.io.File
        import java.io.FileInputStream
        import java.io.FileOutputStream
        import java.nio.file.Files
        import java.nio.file.Paths
        import org.apache.poi.ss.usermodel.CellType
        import org.apache.poi.ss.usermodel.WorkbookFactory
        import org.apache.poi.ss.util.CellRangeAddress
        import org.apache.poi.xssf.usermodel.XSSFWorkbook

        /**
         * @author Excel Operations
         * @created 2025/4/10
         */
        class ExcelFileOperations {
            private companion object {
                private val gson = Gson()
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

            // Create a new Excel file
            @Tool(description = "Create a new Excel file")
            fun createExcelFile(
                @ToolArg(description = "File name with extension") fileName: String,
                @ToolArg(description = "Initial worksheet name") sheetName: String = "Sheet1"
            ): String {
                return try {
                    val workbook = XSSFWorkbook()
                    workbook.createSheet(sheetName)

                    val filePath = if (fileName.endsWith(".xlsx")) fileName else "$fileName.xlsx"
                    FileOutputStream(filePath).use { outputStream ->
                        workbook.write(outputStream)
                    }
                    workbook.close()

                    createSuccessResponse(
                        mapOf(
                            "message" to "Excel file created successfully",
                            "fileName" to filePath,
                            "sheetName" to sheetName
                        )
                    )
                } catch (e: Exception) {
                    createErrorResponse(e.message ?: "Failed to create Excel file")
                }
            }

            // Add a new worksheet
            @Tool(description = "Add a new worksheet")
            fun addWorksheet(
                @ToolArg(description = "Excel file path") filePath: String,
                @ToolArg(description = "Worksheet name") sheetName: String
            ): String {
                return try {
                    val file = File(filePath)
                    if (!file.exists()) {
                        return createErrorResponse("File does not exist: $filePath")
                    }

                    FileInputStream(file).use { inputStream ->
                        val workbook = WorkbookFactory.create(inputStream)

                        // Check if worksheet with same name already exists
                        if (workbook.getSheet(sheetName) != null) {
                            workbook.close()
                            return createErrorResponse("Worksheet '$sheetName' already exists")
                        }

                        workbook.createSheet(sheetName)

                        FileOutputStream(file).use { outputStream ->
                            workbook.write(outputStream)
                        }
                        workbook.close()
                    }

                    createSuccessResponse(
                        mapOf(
                            "message" to "Worksheet added successfully",
                            "filePath" to filePath,
                            "sheetName" to sheetName
                        )
                    )
                } catch (e: Exception) {
                    createErrorResponse(e.message ?: "Failed to add worksheet", mapOf("filePath" to filePath))
                }
            }

            // Delete a worksheet
            @Tool(description = "Delete a worksheet")
            fun deleteWorksheet(
                @ToolArg(description = "Excel file path") filePath: String,
                @ToolArg(description = "Worksheet name to delete") sheetName: String
            ): String {
                return try {
                    val file = File(filePath)
                    if (!file.exists()) {
                        return createErrorResponse("File does not exist: $filePath")
                    }

                    FileInputStream(file).use { inputStream ->
                        val workbook = WorkbookFactory.create(inputStream)
                        val sheetIndex = workbook.getSheetIndex(sheetName)

                        if (sheetIndex < 0) {
                            workbook.close()
                            return createErrorResponse("Worksheet '$sheetName' does not exist")
                        }

                        if (workbook.numberOfSheets <= 1) {
                            workbook.close()
                            return createErrorResponse("Cannot delete the only worksheet, Excel file must have at least one worksheet")
                        }

                        workbook.removeSheetAt(sheetIndex)

                        FileOutputStream(file).use { outputStream ->
                            workbook.write(outputStream)
                        }
                        workbook.close()
                    }

                    createSuccessResponse(
                        mapOf(
                            "message" to "Worksheet deleted successfully",
                            "filePath" to filePath,
                            "sheetName" to sheetName
                        )
                    )
                } catch (e: Exception) {
                    createErrorResponse(e.message ?: "Failed to delete worksheet", mapOf("filePath" to filePath))
                }
            }

            // Rename a worksheet
            @Tool(description = "Rename a worksheet")
            fun renameWorksheet(
                @ToolArg(description = "Excel file path") filePath: String,
                @ToolArg(description = "Current worksheet name") currentName: String,
                @ToolArg(description = "New worksheet name") newName: String
            ): String {
                return try {
                    val file = File(filePath)
                    if (!file.exists()) {
                        return createErrorResponse("File does not exist: $filePath")
                    }

                    FileInputStream(file).use { inputStream ->
                        val workbook = WorkbookFactory.create(inputStream)
                        val sheet = workbook.getSheet(currentName)

                        if (sheet == null) {
                            workbook.close()
                            return createErrorResponse("Worksheet '$currentName' does not exist")
                        }

                        if (workbook.getSheet(newName) != null) {
                            workbook.close()
                            return createErrorResponse("Worksheet name '$newName' is already in use")
                        }

                        workbook.setSheetName(workbook.getSheetIndex(currentName), newName)

                        FileOutputStream(file).use { outputStream ->
                            workbook.write(outputStream)
                        }
                        workbook.close()
                    }

                    createSuccessResponse(
                        mapOf(
                            "message" to "Worksheet renamed successfully",
                            "filePath" to filePath,
                            "oldName" to currentName,
                            "newName" to newName
                        )
                    )
                } catch (e: Exception) {
                    createErrorResponse(e.message ?: "Failed to rename worksheet", mapOf("filePath" to filePath))
                }
            }

            // Write cell data
            @Tool(description = "Write data to a cell")
            fun writeCellData(
                @ToolArg(description = "Excel file path") filePath: String,
                @ToolArg(description = "Worksheet name") sheetName: String,
                @ToolArg(description = "Row index (0-based)") rowIndex: Int,
                @ToolArg(description = "Column index (0-based)") colIndex: Int,
                @ToolArg(description = "Data to write") data: String
            ): String {
                return try {
                    val file = File(filePath)
                    if (!file.exists()) {
                        return createErrorResponse("File does not exist: $filePath")
                    }

                    FileInputStream(file).use { inputStream ->
                        val workbook = WorkbookFactory.create(inputStream)
                        val sheet = workbook.getSheet(sheetName)
                            ?: return createErrorResponse("Worksheet '$sheetName' does not exist")

                        var row = sheet.getRow(rowIndex)
                        if (row == null) {
                            row = sheet.createRow(rowIndex)
                        }

                        val cell = row.createCell(colIndex)
                        cell.setCellValue(data)

                        FileOutputStream(file).use { outputStream ->
                            workbook.write(outputStream)
                        }
                        workbook.close()
                    }

                    createSuccessResponse(
                        mapOf(
                            "message" to "Cell data written successfully",
                            "filePath" to filePath,
                            "sheetName" to sheetName,
                            "cell" to "($rowIndex,$colIndex)",
                            "value" to data
                        )
                    )
                } catch (e: Exception) {
                    createErrorResponse(
                        e.message ?: "Failed to write cell data",
                        mapOf("filePath" to filePath, "sheetName" to sheetName)
                    )
                }
            }

            // Read cell data
            @Tool(description = "Read data from a cell")
            fun readCellData(
                @ToolArg(description = "Excel file path") filePath: String,
                @ToolArg(description = "Worksheet name") sheetName: String,
                @ToolArg(description = "Row index (0-based)") rowIndex: Int,
                @ToolArg(description = "Column index (0-based)") colIndex: Int
            ): String {
                return try {
                    val file = File(filePath)
                    if (!file.exists()) {
                        return createErrorResponse("File does not exist: $filePath")
                    }

                    FileInputStream(file).use { inputStream ->
                        val workbook = WorkbookFactory.create(inputStream)
                        val sheet = workbook.getSheet(sheetName)
                            ?: return createErrorResponse("Worksheet '$sheetName' does not exist")

                        val row = sheet.getRow(rowIndex)
                        if (row == null) {
                            workbook.close()
                            return createSuccessResponse(
                                mapOf(
                                    "value" to "",
                                    "filePath" to filePath,
                                    "sheetName" to sheetName,
                                    "cell" to "($rowIndex,$colIndex)",
                                    "exists" to false
                                )
                            )
                        }

                        val cell = row.getCell(colIndex)
                        val value = when {
                            cell == null -> ""
                            cell.cellType == CellType.NUMERIC -> cell.numericCellValue.toString()
                            cell.cellType == CellType.BOOLEAN -> cell.booleanCellValue.toString()
                            else -> cell.stringCellValue
                        }

                        workbook.close()
                        createSuccessResponse(
                            mapOf(
                                "value" to value,
                                "filePath" to filePath,
                                "sheetName" to sheetName,
                                "cell" to "($rowIndex,$colIndex)",
                                "exists" to true
                            )
                        )
                    }
                } catch (e: Exception) {
                    createErrorResponse(
                        e.message ?: "Failed to read cell data",
                        mapOf("filePath" to filePath, "sheetName" to sheetName)
                    )
                }
            }

            // Batch write row data
            @Tool(description = "Write data to an entire row")
            fun writeRowData(
                @ToolArg(description = "Excel file path") filePath: String,
                @ToolArg(description = "Worksheet name") sheetName: String,
                @ToolArg(description = "Row index (0-based)") rowIndex: Int,
                @ToolArg(description = "Data to write, comma-separated") data: String
            ): String {
                return try {
                    val file = File(filePath)
                    if (!file.exists()) {
                        return createErrorResponse("File does not exist: $filePath")
                    }

                    val values = data.split(",")

                    FileInputStream(file).use { inputStream ->
                        val workbook = WorkbookFactory.create(inputStream)
                        val sheet = workbook.getSheet(sheetName)
                            ?: return createErrorResponse("Worksheet '$sheetName' does not exist")

                        var row = sheet.getRow(rowIndex)
                        if (row == null) {
                            row = sheet.createRow(rowIndex)
                        }

                        for ((colIndex, value) in values.withIndex()) {
                            val cell = row.createCell(colIndex)
                            cell.setCellValue(value.trim())
                        }

                        FileOutputStream(file).use { outputStream ->
                            workbook.write(outputStream)
                        }
                        workbook.close()
                    }

                    createSuccessResponse(
                        mapOf(
                            "message" to "Row data written successfully",
                            "filePath" to filePath,
                            "sheetName" to sheetName,
                            "rowIndex" to rowIndex,
                            "columnCount" to values.size
                        )
                    )
                } catch (e: Exception) {
                    createErrorResponse(
                        e.message ?: "Failed to write row data",
                        mapOf("filePath" to filePath, "sheetName" to sheetName)
                    )
                }
            }

            // Read row data
            @Tool(description = "Read data from an entire row")
            fun readRowData(
                @ToolArg(description = "Excel file path") filePath: String,
                @ToolArg(description = "Worksheet name") sheetName: String,
                @ToolArg(description = "Row index (0-based)") rowIndex: Int
            ): String {
                return try {
                    val file = File(filePath)
                    if (!file.exists()) {
                        return createErrorResponse("File does not exist: $filePath")
                    }

                    FileInputStream(file).use { inputStream ->
                        val workbook = WorkbookFactory.create(inputStream)
                        val sheet = workbook.getSheet(sheetName)
                            ?: return createErrorResponse("Worksheet '$sheetName' does not exist")

                        val row = sheet.getRow(rowIndex)
                        if (row == null) {
                            workbook.close()
                            return createSuccessResponse(
                                mapOf(
                                    "values" to emptyList<String>(),
                                    "filePath" to filePath,
                                    "sheetName" to sheetName,
                                    "rowIndex" to rowIndex,
                                    "exists" to false
                                )
                            )
                        }

                        val values = mutableListOf<String>()
                        for (i in 0 until row.lastCellNum) {
                            val cell = row.getCell(i)
                            val value = when {
                                cell == null -> ""
                                cell.cellType == CellType.NUMERIC -> cell.numericCellValue.toString()
                                cell.cellType == CellType.BOOLEAN -> cell.booleanCellValue.toString()
                                else -> cell.stringCellValue
                            }
                            values.add(value)
                        }

                        workbook.close()
                        createSuccessResponse(
                            mapOf(
                                "values" to values,
                                "filePath" to filePath,
                                "sheetName" to sheetName,
                                "rowIndex" to rowIndex,
                                "columnCount" to values.size,
                                "exists" to true
                            )
                        )
                    }
                } catch (e: Exception) {
                    createErrorResponse(
                        e.message ?: "Failed to read row data",
                        mapOf("filePath" to filePath, "sheetName" to sheetName)
                    )
                }
            }

            // List all worksheets
            @Tool(description = "List all worksheets in an Excel file")
            fun listWorksheets(
                @ToolArg(description = "Excel file path") filePath: String
            ): String {
                return try {
                    val file = File(filePath)
                    if (!file.exists()) {
                        return createErrorResponse("File does not exist: $filePath")
                    }

                    FileInputStream(file).use { inputStream ->
                        val workbook = WorkbookFactory.create(inputStream)
                        val sheetNames = mutableListOf<String>()

                        for (i in 0 until workbook.numberOfSheets) {
                            sheetNames.add(workbook.getSheetName(i))
                        }

                        workbook.close()
                        createSuccessResponse(
                            mapOf(
                                "worksheets" to sheetNames,
                                "count" to sheetNames.size,
                                "filePath" to filePath
                            )
                        )
                    }
                } catch (e: Exception) {
                    createErrorResponse(e.message ?: "Failed to list worksheets", mapOf("filePath" to filePath))
                }
            }

            // Import CSV data into worksheet
            @Tool(description = "Import CSV file data into an Excel worksheet")
            fun importCSV(
                @ToolArg(description = "Excel file path") excelPath: String,
                @ToolArg(description = "CSV file path") csvPath: String,
                @ToolArg(description = "Target worksheet name, creates if not exists") sheetName: String,
                @ToolArg(description = "CSV delimiter (default is comma)") delimiter: String = ","
            ): String {
                return try {
                    val excelFile = File(excelPath)
                    val csvFile = File(csvPath)

                    if (!excelFile.exists()) {
                        return createErrorResponse("Excel file does not exist: $excelPath")
                    }

                    if (!csvFile.exists()) {
                        return createErrorResponse("CSV file does not exist: $csvPath")
                    }

                    val lines = Files.readAllLines(Paths.get(csvPath))
                    if (lines.isEmpty()) {
                        return createErrorResponse("CSV file is empty")
                    }

                    FileInputStream(excelFile).use { inputStream ->
                        val workbook = WorkbookFactory.create(inputStream)

                        var sheet = workbook.getSheet(sheetName)
                        if (sheet == null) {
                            sheet = workbook.createSheet(sheetName)
                        }

                        for ((rowIdx, line) in lines.withIndex()) {
                            val values = line.split(delimiter)
                            val row = sheet.createRow(rowIdx)

                            for ((colIdx, value) in values.withIndex()) {
                                val cell = row.createCell(colIdx)
                                cell.setCellValue(value.trim())
                            }
                        }

                        FileOutputStream(excelFile).use { outputStream ->
                            workbook.write(outputStream)
                        }
                        workbook.close()
                    }

                    createSuccessResponse(
                        mapOf(
                            "message" to "CSV data imported successfully",
                            "excelPath" to excelPath,
                            "csvPath" to csvPath,
                            "sheetName" to sheetName,
                            "rowCount" to lines.size
                        )
                    )
                } catch (e: Exception) {
                    createErrorResponse(
                        e.message ?: "Failed to import CSV",
                        mapOf("excelPath" to excelPath, "csvPath" to csvPath)
                    )
                }
            }

            // Export worksheet data to CSV
            @Tool(description = "Export Excel worksheet to a CSV file")
            fun exportToCSV(
                @ToolArg(description = "Excel file path") excelPath: String,
                @ToolArg(description = "Worksheet name") sheetName: String,
                @ToolArg(description = "CSV output file path") csvPath: String,
                @ToolArg(description = "CSV delimiter (default is comma)") delimiter: String = ","
            ): String {
                return try {
                    val excelFile = File(excelPath)
                    if (!excelFile.exists()) {
                        return createErrorResponse("Excel file does not exist: $excelPath")
                    }

                    FileInputStream(excelFile).use { inputStream ->
                        val workbook = WorkbookFactory.create(inputStream)
                        val sheet = workbook.getSheet(sheetName)
                            ?: return createErrorResponse("Worksheet '$sheetName' does not exist")

                        val csvLines = mutableListOf<String>()

                        for (rowIdx in 0..sheet.lastRowNum) {
                            val row = sheet.getRow(rowIdx) ?: continue
                            val rowValues = mutableListOf<String>()

                            for (colIdx in 0 until row.lastCellNum) {
                                val cell = row.getCell(colIdx)
                                val value = when {
                                    cell == null -> ""
                                    cell.cellType == CellType.NUMERIC -> cell.numericCellValue.toString()
                                    cell.cellType == CellType.BOOLEAN -> cell.booleanCellValue.toString()
                                    else -> cell.stringCellValue
                                }
                                rowValues.add(value)
                            }

                            csvLines.add(rowValues.joinToString(delimiter))
                        }

                        workbook.close()

                        // Write to CSV file
                        Files.write(Paths.get(csvPath), csvLines)

                        createSuccessResponse(
                            mapOf(
                                "message" to "Worksheet exported successfully",
                                "excelPath" to excelPath,
                                "csvPath" to csvPath,
                                "sheetName" to sheetName,
                                "rowCount" to csvLines.size
                            )
                        )
                    }
                } catch (e: Exception) {
                    createErrorResponse(
                        e.message ?: "Failed to export to CSV",
                        mapOf("excelPath" to excelPath, "sheetName" to sheetName)
                    )
                }
            }

            // Merge cells
            @Tool(description = "Merge a range of cells")
            fun mergeCells(
                @ToolArg(description = "Excel file path") filePath: String,
                @ToolArg(description = "Worksheet name") sheetName: String,
                @ToolArg(description = "First row index (0-based)") firstRow: Int,
                @ToolArg(description = "Last row index (0-based)") lastRow: Int,
                @ToolArg(description = "First column index (0-based)") firstCol: Int,
                @ToolArg(description = "Last column index (0-based)") lastCol: Int
            ): String {
                return try {
                    val file = File(filePath)
                    if (!file.exists()) {
                        return createErrorResponse("File does not exist: $filePath")
                    }

                    FileInputStream(file).use { inputStream ->
                        val workbook = WorkbookFactory.create(inputStream)
                        val sheet = workbook.getSheet(sheetName)
                            ?: return createErrorResponse("Worksheet '$sheetName' does not exist")

                        sheet.addMergedRegion(CellRangeAddress(firstRow, lastRow, firstCol, lastCol))

                        FileOutputStream(file).use { outputStream ->
                            workbook.write(outputStream)
                        }
                        workbook.close()
                    }

                    createSuccessResponse(
                        mapOf(
                            "message" to "Cells merged successfully",
                            "filePath" to filePath,
                            "sheetName" to sheetName,
                            "range" to "($firstRow,$firstCol):($lastRow,$lastCol)"
                        )
                    )
                } catch (e: Exception) {
                    createErrorResponse(
                        e.message ?: "Failed to merge cells",
                        mapOf("filePath" to filePath, "sheetName" to sheetName)
                    )
                }
            }
        }
