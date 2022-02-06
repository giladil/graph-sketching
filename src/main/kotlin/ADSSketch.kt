import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.*
import java.io.File

object ADSSketch {
    fun buildADSSketchForGraph(k: Int, graph: Map<String, MutableList<GraphEdge>>): Map<String, List<ADSEntry>> {
        println("starting ADS sketch")
        val nodes = graph.keys.sortedBy { hashFunction(it) } // Nodes by increasing hash value
        val adsSketch = mutableMapOf<String, MutableList<ADSEntry>>()
        var count = 0
        for (node in nodes) {
            prunedDijkstra(node, graph, adsSketch, k)
            count += 1
            if (count % 100 == 0) {
                println("Finished $count out of ${graph.size}")
            }
        }
        return adsSketch
    }

    // For node v, do a pruned dikjstra for it, updating the adsSketch as it goes (in place!)
    private fun prunedDijkstra(
        node: String,
        graph: Map<String, MutableList<GraphEdge>>,
        adsSketch: MutableMap<String, MutableList<ADSEntry>>,
        k: Int
    ) {
        val nodesToExplore = graph.keys.toMutableSet()
        val distMap = mutableMapOf<String, Double>()
        distMap[node] = 0.0
        while (nodesToExplore.isNotEmpty()) {
            val nextNode = dikjstraMinDist(distMap, nodesToExplore)
            nodesToExplore.remove(nextNode)
            val dvu = distMap[nextNode] ?: continue
            updateDistMap(graph, nextNode, nodesToExplore, distMap)
            // Update sketch
            val sketch = adsSketch.getOrDefault(nextNode, mutableListOf())
            if (!sketch.contains(node) && sketch.sizeOfCloser(dvu) < k) {
                sketch.add(
                    ADSEntry(
                        node = node,
                        hashValue = hashFunction(node),
                        dist = dvu,
                        p = sketch.calculateP(k)
                    )
                )
                adsSketch[nextNode] = sketch
            } else {
                break
            }
        }
    }

    private fun dikjstraMinDist(distMap: Map<String, Double>, nodesToExplore: MutableSet<String>): String {
        var minDist = Double.MAX_VALUE
        var minNode = nodesToExplore.first()

        for (node in nodesToExplore) {
            val dist = distMap.getOrDefault(node, Double.MAX_VALUE)
            if (dist < minDist) {
                minDist = dist
                minNode = node
            }
        }

        return minNode
    }


    fun fullAllDistance(graph: Map<String, MutableList<GraphEdge>>) = runBlocking {
        coroutineScope {
            println("starting full all distance")
            val distMap = mutableMapOf<String, Map<String, Double>>()
            var count = 0
            val chunkSize = 100
            val maxJobs = 5
            val jobs: MutableList<Deferred<Unit>> = mutableListOf()
            var part = 1
            val FILE_PRFIX = "src/main/resources"

            graph.keys.chunked(chunkSize).forEach {
                jobs.add(async(Dispatchers.Default) { processDijkstraBatch(it, graph, distMap) })
                if (jobs.size == maxJobs) {
                    jobs.awaitAll()
                    jobs.clear()
                    count += (maxJobs * chunkSize)
                    println("Finished $count out of ${graph.size}")
                    if (count % 2000 == 0) {
                        val allDistanceJson = ObjectMapper().writeValueAsString(distMap)
                        File("$FILE_PRFIX/epinions-data-full-all-distance-part-${part}.json").writeText(allDistanceJson)
                        part += 1
                        distMap.clear()
                    }
                }
            }
            if (jobs.size != 0) {
                jobs.awaitAll()
                count += (jobs.size * chunkSize)
                jobs.clear()
                println("Finished $count out of ${graph.size}")
                val allDistanceJson = ObjectMapper().writeValueAsString(distMap)
                File("$FILE_PRFIX/epinions-data-full-all-distance-part-${part}.json").writeText(allDistanceJson)
                distMap.clear()
            }
        }
    }


    private fun processDijkstraBatch(
        keys: List<String>,
        graph: Map<String, MutableList<GraphEdge>>,
        distMap: MutableMap<String, Map<String, Double>>
    ) {
        for (node in keys) {
            val nodeResult = fullDijkstra(node, graph)
            distMap[node] = nodeResult
        }
    }

    // For node v, do a pruned dikjstra for it, updating the adsSketch as it goes (in place!)
    private fun fullDijkstra(
        node: String,
        graph: Map<String, MutableList<GraphEdge>>
    ): Map<String, Double> {
        val nodesToExplore = graph.keys.toMutableSet()
        val distMap = mutableMapOf<String, Double>()
        distMap[node] = 0.0
        while (nodesToExplore.isNotEmpty()) {
            val nextNode = dikjstraMinDist(distMap, nodesToExplore)
            nodesToExplore.remove(nextNode)
            updateDistMap(graph, nextNode, nodesToExplore, distMap)
        }

        return distMap
    }

    private fun updateDistMap(
        graph: Map<String, MutableList<GraphEdge>>,
        nextNode: String,
        nodesToExplore: MutableSet<String>,
        distMap: MutableMap<String, Double>
    ) {
        for (edge in graph.getOrDefault(nextNode, listOf())) {
            val neighbor = edge.dest
            if (neighbor !in nodesToExplore) {
                continue
            }
            val alt = if (distMap[nextNode] == null) Double.MAX_VALUE else distMap[nextNode]!! + edge.weight
            if (alt < distMap.getOrDefault(neighbor, Double.MAX_VALUE)) {
                distMap[neighbor] = alt
            }
        }
    }
}