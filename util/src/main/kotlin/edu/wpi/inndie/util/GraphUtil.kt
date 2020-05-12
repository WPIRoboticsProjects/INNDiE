@file:Suppress("UnstableApiUsage")

package edu.wpi.inndie.util

import arrow.Kind2
import arrow.core.ForEither
import arrow.core.Left
import arrow.core.Right
import com.google.common.graph.Graph
import edu.wpi.inndie.logging.joinWithIndent

/**
 * Checks if there are islands in this graph.
 *
 * @return True if there are islands.
 */
fun <T : Any> Graph<T>.checkIslands(): Kind2<ForEither, String, Unit> {
    val allNodes = nodes()
    if (allNodes.isEmpty()) {
        // Can't have islands with an empty graph.
        return Right(Unit)
    }

    // BFS should find every node if there is only one island. Starting node does not matter.
    // Supply a custom nextNode function because the direction BFS moves in is irrelevant,
    // it just has to touch every node in an island.
    val reachable = breadthFirstSearch(allNodes.first()) { successors(it) + predecessors(it) }
    return if (reachable == allNodes) {
        Right(Unit)
    } else {
        Left(
            """
            |The following nodes are not reachable:
            |${(allNodes - reachable).joinWithIndent("\t")}
            """.trimMargin()
        )
    }
}

/**
 * Finds if there are any circuits starting from [root].
 *
 * @param root The node to start from.
 * @param visited The nodes visited so far (used in the recursive call).
 * @return True if there are circuits.
 */
fun <T : Any> Graph<T>.hasCircuits(
    root: T,
    visited: Set<T> = emptySet()
): Boolean {
    successors(root).forEach { node ->
        val reachable = breadthFirstSearch(node)

        // Finding an already visited node in the new reachable set implies a circuit. The
        // reachable set will strictly decrease for successor nodes in a DAG.
        if (visited anyIn reachable || hasCircuits(node, visited + node)) {
            return true
        }
    }

    return false
}

/**
 * Runs a breadth first search (with no target node) starting at [root] and moving using the
 * [nextNodes] successor function.
 *
 * @param root The node to start searching from.
 * @param nextNodes A successor function.
 * @return All the visited nodes.
 */
fun <T : Any> Graph<T>.breadthFirstSearch(
    root: T,
    nextNodes: Graph<T>.(T) -> Set<T> = Graph<T>::successors
): Set<T> {
    val visited = mutableSetOf<T>()
    val queue = mutableListOf<T>()

    visited += root
    queue += root

    while (queue.isNotEmpty()) {
        val node = queue.first()
        queue.remove(node)

        nextNodes(node).forEach {
            if (it !in visited) {
                visited += it
                queue += it
            }
        }
    }

    return visited
}
