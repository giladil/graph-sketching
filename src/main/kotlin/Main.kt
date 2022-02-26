import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

fun main() {
    val FILE_PRFIX = "src/main/outputs"
    val filename = "etherium-100k-fix.csv"

    var graph = readFile(filename, ',', false)
    val reachabilitySet= GraphReachability.buildReachabilitySet(graph, false,  20, "etherium")
    val reachJson = ObjectMapper().writeValueAsString(reachabilitySet)
    File("$FILE_PRFIX/etherium-reach.json").writeText(reachJson)

    val reachabilitySetSketch = GraphReachability.buildReachabilitySet(graph, true, 20)
    val sketchJson = ObjectMapper().writeValueAsString(reachabilitySetSketch)
    File("$FILE_PRFIX/etherium-reach-sketch.json").writeText(sketchJson)


//     ADS
    val reverseGraph = readFile(filename, ',', true)
    var adsSketch = ADSSketch.buildADSSketchForGraph(15, reverseGraph)
    var adsJson = ObjectMapper().writeValueAsString(adsSketch)
    File("$FILE_PRFIX/etherium-ads-15.json").writeText(adsJson)

    adsSketch = ADSSketch.buildADSSketchForGraph(2, reverseGraph)
    adsJson = ObjectMapper().writeValueAsString(adsSketch)
    File("$FILE_PRFIX/etherium-ads-2.json").writeText(adsJson)

    adsSketch = ADSSketch.buildADSSketchForGraph(5, reverseGraph)
    adsJson = ObjectMapper().writeValueAsString(adsSketch)
    File("$FILE_PRFIX/etherium-ads-5.json").writeText(adsJson)

    adsSketch = ADSSketch.buildADSSketchForGraph(20, reverseGraph)
    adsJson = ObjectMapper().writeValueAsString(adsSketch)
    File("$FILE_PRFIX/etherium-ads-20.json").writeText(adsJson)

    adsSketch = ADSSketch.buildADSSketchForGraph(1, reverseGraph)
    adsJson = ObjectMapper().writeValueAsString(adsSketch)
    File("$FILE_PRFIX/etherium-ads-1.json").writeText(adsJson)

    adsSketch = ADSSketch.buildADSSketchForGraph(10, reverseGraph)
    adsJson = ObjectMapper().writeValueAsString(adsSketch)
    File("$FILE_PRFIX/etherium-ads-10.json").writeText(adsJson)


    graph = readFile(filename, ',', false)
    ADSSketch.fullAllDistance(graph, "etherium-all-distance")
}