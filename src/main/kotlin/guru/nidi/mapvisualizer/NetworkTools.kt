package guru.nidi.mapvisualizer

import guru.nidi.mapvisualizer.FileTools.ifNotExist
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import java.io.File
import java.io.IOException
import java.nio.file.Files

object NetworkTools {
    private val client = HttpClientBuilder.create().build()

    fun download(name: String, url: String): File {
        return File(FileTools.dir, name).ifNotExist { file ->
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
}