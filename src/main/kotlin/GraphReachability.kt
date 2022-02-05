import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

object GraphReachability {

    fun buildReachabilitySet(
        graph: Map<String, MutableList<GraphEdge>>,
        sketch: Boolean,
        k: Int = 5
    ): Map<String, List<String>> =
        runBlocking {
            return@runBlocking coroutineScope {
                val reachSet = mutableMapOf<String, List<String>>()
                var count = 0
                val CHUNK_SIZE = 300
                val jobs: MutableList<Deferred<Map<String, List<String>>>> = mutableListOf()

                graph.keys.chunked(CHUNK_SIZE).forEach {
                    jobs.add(async { reachabilityChunks(it, graph, sketch, k) })
                    if (jobs.size == 2) {
                        val partials = jobs.awaitAll()
                        for (partial in partials) {
                            reachSet.putAll(partial)
                            count += CHUNK_SIZE
                            println("finished $count out of ${graph.size}")
                        }
                        jobs.clear()
                    }
                }

                return@coroutineScope reachSet
            }
        }

    private fun reachabilityChunks(
        nodes: List<String>,
        graph: Map<String, MutableList<GraphEdge>>,
        sketch: Boolean = false,
        k: Int
    ): Map<String, List<String>> {
        val reachSet = mutableMapOf<String, List<String>>()
        for (node in nodes) {
            if (sketch) {
                reachSet[node] = kMinsReachability(k, node, graph)
            } else {
                reachSet[node] = fullNodeReachability(node, graph)
            }
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
