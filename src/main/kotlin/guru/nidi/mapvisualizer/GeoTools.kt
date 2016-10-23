package guru.nidi.mapvisualizer

import com.vividsolutions.jts.geom.Geometry
import guru.nidi.mapvisualizer.FileTools.ifNotExist
import org.geotools.data.DataUtilities
import org.geotools.data.simple.SimpleFeatureCollection
import org.geotools.data.simple.SimpleFeatureSource
import org.geotools.geojson.feature.FeatureJSON
import org.geotools.geometry.jts.JTS
import org.geotools.referencing.CRS
import java.io.File


object GeoTools {
    fun projectFeatures(featureSource: SimpleFeatureSource, targetCRS: String): SimpleFeatureCollection {
        val memory = DataUtilities.collection(featureSource.features)
        val transform = CRS.findMathTransform(featureSource.schema.coordinateReferenceSystem, CRS.decode(targetCRS), true)
        val iter = memory.features()
        while (iter.hasNext()) {
            val feature = iter.next()
            val geometry: Geometry = feature.defaultGeometry as Geometry
            feature.defaultGeometry = JTS.transform(geometry, transform)
        }
        return memory
    }

    fun writeAsGeoJson(name: String, features: SimpleFeatureCollection): File {
        return FileTools.file(name + ".json").ifNotExist { file ->
            file.writer().use { out ->
                val fj = FeatureJSON()
                fj.isEncodeFeatureCollectionBounds = true
                fj.writeFeatureCollection(features, out)
            }
        }
    }
}