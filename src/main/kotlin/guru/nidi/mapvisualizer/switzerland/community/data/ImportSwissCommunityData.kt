package guru.nidi.mapvisualizer.switzerland.community.data

import guru.nidi.mapvisualizer.*
import guru.nidi.mapvisualizer.ConversionTools.asDouble
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.FileInputStream
import java.io.InputStreamReader
import java.time.LocalDate
import java.util.*

object ImportSwissCommunityData {
    @JvmStatic
    fun main(args: Array<String>) {
        fun sublist(r: CSVRecord): List<String> {
            val asList = r.toList()
            return asList.subList(2, asList.size)
        }

        val download = NetworkTools.download("swiss-community-data.xls", "https://www.bfs.admin.ch/bfsstatic/dam/assets/328115/master")
        val data = ConversionTools.xlsToCsv(download)
        InputStreamReader(FileInputStream(data), "utf-8").use { inp ->
            val series = ArrayList<String>()
            val values = HashMap<Int, List<Double?>>()
            for (record in CSVFormat.EXCEL.parse(inp)) {
                if (record.recordNumber.toInt() == 6) series.addAll(sublist(record))
                if (record.recordNumber > 9)
                    try {
                        values[record[0].toInt()] = sublist(record).map { it.asDouble() }
                    } catch (e: NumberFormatException) {
                        //no valid id -> stop parsing
                        break
                    }
            }
            FileTools.writeData("swiss-community-data", Data("Regionalporträts Schweizer Gemeinden", series, listOf(Datum(LocalDate.of(2016, 1, 1), values))))
        }
    }
}