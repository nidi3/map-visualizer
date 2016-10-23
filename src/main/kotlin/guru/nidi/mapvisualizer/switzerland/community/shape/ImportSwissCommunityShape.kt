package guru.nidi.mapvisualizer.switzerland.community.shape

import guru.nidi.mapvisualizer.FileTools.unzip
import guru.nidi.mapvisualizer.GeoTools
import guru.nidi.mapvisualizer.NetworkTools
import org.geotools.data.FileDataStoreFinder
import java.io.File

object ImportSwissCommunityShape {
    @JvmStatic
    fun main(args: Array<String>) {
        load(15, "https://www.bfs.admin.ch/bfsstatic/dam/assets/330759/master")
        load(16, "https://www.bfs.admin.ch/bfsstatic/dam/assets/453578/master")
    }

    fun load(year: Int, url: String) {
        val download = NetworkTools.download("swiss-community-shape-$year.zip", url)
        val shapes = unzip(download)
        val ggg = unzip(File(shapes, "ggg_$year.zip"))
        val store = FileDataStoreFinder.getDataStore(File(ggg, "shp/g1g$year.shp"))
        val projected = GeoTools.projectFeatures(store.featureSource, "EPSG:3857")
        GeoTools.writeAsGeoJson("swiss-community-shape-$year", projected)
    }

}