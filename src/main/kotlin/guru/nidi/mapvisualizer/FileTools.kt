package guru.nidi.mapvisualizer

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.util.zip.ZipInputStream

object FileTools {
    val dir = File(System.getProperty("java.io.tmpdir"), "map-visualizer")
    val mapper = ObjectMapper()

    init {
        dir.mkdirs()
    }

    fun file(name: String): File {
        return File(dir, name)
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

    fun writeData(name: String, data: Data): File {
        return File(dir, name + ".json").ifNotExist { dest ->
            mapper.writeValue(dest, data)
        }
    }

    fun File.withExtension(extension: String): File {
        return File(parentFile, name.substring(0, name.lastIndexOf('.')) + extension)
    }

    fun File.ifNotExist(action: (File) -> Unit): File {
        if (!exists()) {
            action(this)
        }
        return this
    }

}

