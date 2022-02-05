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

fun List<ADSEntry>.calculateP(k: Int): Double {
    if (this.size < k) {
        return 1.0
    }
    val maxCurrentValue = this.maxOf { it.hashValue }
    return maxCurrentValue.toDouble() / Int.MAX_VALUE
}