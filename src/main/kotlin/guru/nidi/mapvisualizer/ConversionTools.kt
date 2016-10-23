package guru.nidi.mapvisualizer

import guru.nidi.mapvisualizer.FileTools.ifNotExist
import guru.nidi.mapvisualizer.FileTools.withExtension
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat

object ConversionTools {
    fun xlsToCsv(file: File): File {
        return file.withExtension(".csv").ifNotExist { dest ->
            CSVFormat.EXCEL.print(OutputStreamWriter(FileOutputStream(dest), "utf-8")).use { out ->
                NPOIFSFileSystem(file).use { fs ->
                    convert(HSSFWorkbook(fs.root, true), out)
                }
            }
        }
    }

    fun String.asDouble(): Double? {
        try {
            return toDouble()
        } catch(e: NumberFormatException) {
            return null
        }
    }

    private fun convert(wb: HSSFWorkbook, out: CSVPrinter) {
        val sheet = wb.getSheetAt(0) //TODO handle multisheet
        for (y in 0..sheet.lastRowNum) {
            val row = sheet.getRow(y)
            if (row != null) {
                for (x in 0..row.lastCellNum) {
                    out.print(cellValue(row.getCell(x)))
                }
                out.println()
            }
        }
    }

    private fun cellValue(c: HSSFCell?): String {
        return if (c == null) ""
        else when (c.cellTypeEnum) {
            CellType.STRING -> c.richStringCellValue.string
            CellType.NUMERIC ->
                if (DateUtil.isCellDateFormatted(c)) convertDate(c)
                else convertNumber(c)
            else -> ""
        }
    }

    private fun convertDate(c: HSSFCell): String {
        return SimpleDateFormat("YYYY-dd-MM").format(c.dateCellValue)
    }

    private fun convertNumber(c: HSSFCell): String {
        val s = c.numericCellValue.toString()
        return if (s.endsWith(".0")) s.substring(0, s.length - 2) else s
    }
}