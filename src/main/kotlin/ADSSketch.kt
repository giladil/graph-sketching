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
            // Update sketch
            val sketch = adsSketch.getOrDefault(nextNode, mutableListOf())
            if (!sketch.contains(node) && sketch.sizeOfCloser(dvu) < k) {
                sketch.add(
                    ADSEntry(
                        node = node,
                        hashValue = hashFunction(node),
                        dist = dvu,
                        p = 1.0 // TODO FIX THIS
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
}