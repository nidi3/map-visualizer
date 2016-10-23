package guru.nidi.mapvisualizer

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import java.io.*
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.zip.ZipInputStream

object Tools {
    val dir = File(System.getProperty("java.io.tmpdir"), "map-visualizer")
    val client = HttpClientBuilder.create().build()
    val mapper = ObjectMapper()

    init {
        dir.mkdirs()
    }

    fun file(name: String): File {
        return File(dir, name)
    }

    fun download(name: String, url: String): File {
        return File(dir, name).ifNotExist { file ->
            val get = HttpGet(url)
            client.execute(get).use { response ->
                val status = response.statusLine
                if (status.statusCode !== 200) {
                    throw IOException("Could not download $url. ${status.statusCode}: ${status.reasonPhrase}")
                }
                Files.copy(response.entity.content, file.toPath())
            }
        }
    }

    fun unzip(file: File): File {
        return file.withExtension("").ifNotExist { destDir ->
            destDir.mkdir();
            ZipInputStream(FileInputStream(file)).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val file = File(destDir, entry.name)
                    if (entry.isDirectory) {
                        file.mkdir()
                    } else {
                        file.parentFile.mkdirs()
                        Files.copy(zip, file.toPath())
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }
    }

    fun convertXlsToCsv(file: File): File {
        fun convertDate(c: HSSFCell) = SimpleDateFormat("YYYY-dd-MM").format(c.dateCellValue)
        fun convertNumber(c: HSSFCell): String {
            val s = c.numericCellValue.toString()
            return if (s.endsWith(".0")) s.substring(0, s.length - 2) else s
        }

        fun cellValue(c: HSSFCell?): String {
            return if (c == null) ""
            else when (c.cellTypeEnum) {
                CellType.STRING -> c.richStringCellValue.string
                CellType.NUMERIC ->
                    if (DateUtil.isCellDateFormatted(c)) convertDate(c)
                    else convertNumber(c)
                else -> ""
            }
        }

        fun convert(wb: HSSFWorkbook, out: CSVPrinter) {
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

        return file.withExtension(".csv").ifNotExist { dest ->
            CSVFormat.EXCEL.print(OutputStreamWriter(FileOutputStream(dest), "utf-8")).use { out ->
                NPOIFSFileSystem(file).use { fs ->
                    convert(HSSFWorkbook(fs.root, true), out)
                }
            }
        }
    }


    fun writeData(name: String, data: Data): File {
        return File(dir, name + ".json").ifNotExist { dest ->
            mapper.writeValue(dest, data)
        }
    }

    fun String.asDouble(): Double? {
        try {
            return toDouble()
        } catch(e: NumberFormatException) {
            return null
        }
    }

    private fun File.withExtension(extension: String): File {
        return File(parentFile, name.substring(0, name.lastIndexOf('.')) + extension)
    }

    private fun File.ifNotExist(action: (File) -> Unit): File {
        if (!exists()) {
            action(this)
        }
        return this
    }

}

