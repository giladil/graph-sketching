import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

fun main() {
    val FILE_PRFIX = "src/main/resources"

//    val graph = readFile("soc-epinions.mtx", ' ', false)
//    val reachabilitySet = GraphReachability.buildReachabilitySet(graph, false)
//    val json = ObjectMapper().writeValueAsString(reachabilitySet)
//    File("$FILE_PRFIX/epinions-data.json").writeText(json)
//    val reachabilitySetSketch = GraphReachability.buildReachabilitySet(graph, true, 5)
//    val sketchJson = ObjectMapper().writeValueAsString(reachabilitySetSketch)
//    File("$FILE_PRFIX/epinions-sketch.json").writeText(sketchJson)


    // ADS
    val reverseGraph = readFile("soc-epinions.mtx", ' ', true)
    val adsSketch = ADSSketch.buildADSSketchForGraph(2, reverseGraph)
    val adsJson = ObjectMapper().writeValueAsString(adsSketch)
    File("$FILE_PRFIX/epinions-data-ads.json").writeText(adsJson)

    val graph= readFile("soc-epinions.mtx", ' ', false)
    val allDistance = ADSSketch.fullAllDistance(graph)
    val allDistanceJson = ObjectMapper().writeValueAsString(adsSketch)
    File("$FILE_PRFIX/epinions-data-full-all-distance.json").writeText(allDistanceJson)
}