import java.io.File
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.math.abs

fun hashFunction(node: String): Int {
    val md5 = MessageDigest.getInstance("MD5")
    val bytes = node.toByteArray()
    val hashedBytes = md5.digest(bytes).take(Int.SIZE_BYTES)
    return abs(ByteBuffer.wrap(hashedBytes.toByteArray()).int)
}

fun readFile(fileName: String, seperator: Char, reverse: Boolean): Map<String, MutableList<GraphEdge>> {
    val fileUrl = object {}.javaClass.getResource("/$fileName")!!.toURI()
    val file = File(fileUrl)
    val graph = mutableMapOf<String, MutableList<GraphEdge>>()
    file.forEachLine { line ->
        val split = line.strip().split(seperator)
        val source = if (reverse) split[1] else split[0]
        val dest = if (reverse) split[0] else split[1]
        val weight = split.getOrNull(2)?.toDouble() ?: 1.0
        val nodes = graph.getOrDefault(source, mutableListOf())
        nodes.add(GraphEdge(source = source, dest = dest, weight = weight))
        graph[source] = nodes
    }

    return graph
}