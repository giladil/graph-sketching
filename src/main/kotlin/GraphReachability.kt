import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

object GraphReachability {
    val FILE_PRFIX = "src/main/outputs"

    fun buildReachabilitySet(
        graph: Map<String, MutableList<GraphEdge>>,
        sketch: Boolean,
        k: Int = 5,
        filename: String? = null
    ): Map<String, List<String>> {
        val reachSet = mutableMapOf<String, List<String>>()
        var count = 0
        val total = graph.size
        var part = 0

        for (node in graph.keys) {
            if (sketch) {
                reachSet[node] = kMinsReachability(k, node, graph)
            } else {
                reachSet[node] = fullNodeReachability(node, graph)
            }
            count += 1
            if (count % 100 == 0) { println("finished $count out of $total") }
            if (count % 10000 == 0  && filename != null) {
                val reachJson = ObjectMapper().writeValueAsString(reachSet)
                File("$FILE_PRFIX/$filename-reach-part-$part.json").writeText(reachJson)
                part += 1
                reachSet.clear()
            }
        }

        if (filename != null) {
            val reachJson = ObjectMapper().writeValueAsString(reachSet)
            File("$FILE_PRFIX/$filename-reach-part-$part.json").writeText(reachJson)
            reachSet.clear()
        }
        return reachSet
    }

    private fun fullNodeReachability(node: String, graph: Map<String, MutableList<GraphEdge>>): List<String> {
        val nodeReachability = hashSetOf(node)
        val nodesToDiscover = mutableListOf(node)
        while (nodesToDiscover.isNotEmpty()) {
            val newNode = nodesToDiscover.removeFirst()
            for (edge in graph.getOrDefault(newNode, listOf())) {
                val neighbor = edge.dest
                if (!nodeReachability.contains(neighbor)) {
                    nodeReachability.add(neighbor)
                    nodesToDiscover.add(neighbor)
                }
            }
        }

        return nodeReachability.toList()
    }

    private fun kMinsReachability(k: Int, node: String, graph: Map<String, MutableList<GraphEdge>>): List<String> {
        val seenNodes = hashSetOf(node)
        val kMins = mutableListOf(hashFunction(node))
        val nodesToDiscover = mutableListOf(node)

        while (nodesToDiscover.isNotEmpty()) {
            val newNode = nodesToDiscover.removeFirst()
            for (edge in graph.getOrDefault(newNode, listOf())) {
                val neighbor = edge.dest
                if (!seenNodes.contains(neighbor)) {
                    seenNodes.add(neighbor)
                    nodesToDiscover.add(neighbor)
                    val hashValue = hashFunction(neighbor)
                    if (kMins.size < k) {
                        kMins.add(hashValue)
                    } else {
                        val maxValue = kMins.maxOrNull()!!
                        if (hashValue < maxValue) {
                            kMins.remove(maxValue)
                            kMins.add(hashValue)
                        }
                    }
                }
            }
        }

        return kMins.map { it.toString() }
    }
}
