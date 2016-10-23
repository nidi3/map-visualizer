package guru.nidi.mapvisualizer.switzerland.community.shape

import guru.nidi.mapvisualizer.Tools
import org.geotools.data.FileDataStoreFinder
import org.geotools.geojson.feature.FeatureJSON
import java.io.File

object ImportSwissCommunityShape {
    @JvmStatic
    fun main(args: Array<String>) {
        val download = Tools.download("swiss-community-shape.zip", "https://www.bfs.admin.ch/bfsstatic/dam/assets/453578/master")
        val shapes = Tools.unzip(download)
        val ggg = Tools.unzip(File(shapes, "ggg_16.zip"))
        val store = FileDataStoreFinder.getDataStore(File(ggg, "shp/g1g16.shp"))
        val featureSource = store.featureSource
        Tools.file("swiss-community-shape.json").writer().use { out ->
            FeatureJSON().writeFeatureCollection(featureSource.features, out)
        }
    }
}