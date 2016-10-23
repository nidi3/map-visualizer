package guru.nidi.mapvisualizer

import java.time.temporal.Temporal

data class Data(val title: String, val series: List<String>, val data: List<Datum>)
data class Datum(val time: Temporal, val values: Map<out Any, List<Any?>>)
