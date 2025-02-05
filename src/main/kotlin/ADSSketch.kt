import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.*
import java.io.File
import java.util.PriorityQueue


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
        val queue = PriorityQueue(compareBy<Pair<String, Double>> { it.second })

        val seenElements = mutableSetOf<String>()
        nodesToExplore.forEach {
            if (it == node) {
                queue.add(Pair(it, 0.0))
            } else {
                queue.add(Pair(it, Double.MAX_VALUE))
            }
        }
        while (nodesToExplore.isNotEmpty()) {
            var hasSeen = true
            var nextNode = ""
            while (hasSeen) {
                nextNode = queue.remove().first
                hasSeen = seenElements.contains(nextNode)
            }
            nodesToExplore.remove(nextNode)
            val dvu = distMap[nextNode] ?: continue
            updateDistMap(graph, nextNode, nodesToExplore, distMap, queue)

            seenElements.add(nextNode)

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

    fun fullAllDistance(graph: Map<String, MutableList<GraphEdge>>, filename: String) {
        println("starting full all distance")
        val distMap = mutableMapOf<String, Map<String, Double>>()
        var count = 0
        var part = 1
        val FILE_PRFIX = "src/main/outputs"
        val total = graph.size

        for (node in graph.keys) {
            val nodeResult = fullDijkstra(node, graph)
            distMap[node] = nodeResult
            count += 1
            if (count % 100 == 0) {
                println("finished $count out of $total")
            }
            if (count % 2000 == 0) {
                val allDistanceJson = ObjectMapper().writeValueAsString(distMap)
                File("$FILE_PRFIX/$filename-part-${part}.json").writeText(allDistanceJson)
                part += 1
                distMap.clear()
            }
        }
        val allDistanceJson = ObjectMapper().writeValueAsString(distMap)
        File("$FILE_PRFIX/$filename-part-${part}.json").writeText(allDistanceJson)
        distMap.clear()
    }


    // For node v, do a pruned dikjstra for it, updating the adsSketch as it goes (in place!)
    private fun fullDijkstra(
        node: String,
        graph: Map<String, MutableList<GraphEdge>>
    ): Map<String, Double> {
        val nodesToExplore = graph.keys.toMutableSet()
        val distMap = mutableMapOf<String, Double>()
        distMap[node] = 0.0
        val queue = PriorityQueue(compareBy<Pair<String, Double>> { it.second })

        val seenElements = mutableSetOf<String>()
        nodesToExplore.forEach {
            if (it == node) {
                queue.add(Pair(it, 0.0))
            } else {
                queue.add(Pair(it, Double.MAX_VALUE))
            }
        }
        while (nodesToExplore.isNotEmpty()) {
            var hasSeen = true
            var nextNode = ""
            while (hasSeen) {
                nextNode = queue.remove().first
                hasSeen = seenElements.contains(nextNode)
            }
            nodesToExplore.remove(nextNode)
            updateDistMap(graph, nextNode, nodesToExplore, distMap, queue)
        }

        return distMap
    }

    private fun updateDistMap(
        graph: Map<String, MutableList<GraphEdge>>,
        nextNode: String,
        nodesToExplore: MutableSet<String>,
        distMap: MutableMap<String, Double>,
        queue: PriorityQueue<Pair<String, Double>>
    ) {
        for (edge in graph.getOrDefault(nextNode, listOf())) {
            val neighbor = edge.dest
            if (neighbor !in nodesToExplore) {
                continue
            }
            val alt = if (distMap[nextNode] == null) Double.MAX_VALUE else distMap[nextNode]!! + edge.weight
            if (alt < distMap.getOrDefault(neighbor, Double.MAX_VALUE)) {
                distMap[neighbor] = alt
                queue.add(Pair(neighbor, alt))
            }
        }
    }
}