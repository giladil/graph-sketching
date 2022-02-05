data class GraphEdge(
    val source: String,
    val dest: String,
    val weight: Double
)

data class ADSEntry(
    val node: String,
    val hashValue: Int,
    val dist: Double,
    val p: Double
)

fun List<ADSEntry>.contains(node: String): Boolean {
    for (entry in this) {
        if (entry.node == node) {
            return true
        }
    }
    return false
}

fun List<ADSEntry>.sizeOfCloser(dist: Double): Int {
    var count = 0
    for (entry in this) {
        if (entry.dist < dist) {
            count += 1
        }
    }
    return count
}