package edu.wpi.axon.core.dsl

import com.google.common.graph.GraphBuilder
import com.google.common.graph.ImmutableGraph
import com.google.common.graph.MutableGraph
import edu.wpi.axon.core.dsl.container.PolymorphicNamedDomainObjectContainer
import edu.wpi.axon.core.dsl.variable.Code

/**
 * Parses a [PolymorphicNamedDomainObjectContainer] of [Code] into an [ImmutableGraph], assuring
 * that all [Code] nodes are unique and dependencies are properly ordered.
 */
@Suppress("UnstableApiUsage")
class CodeGraph(
    private val container: PolymorphicNamedDomainObjectContainer<Code>
) {

    val graph: ImmutableGraph<Code> by lazy {
        val mutableGraph = GraphBuilder.directed()
            .allowsSelfLoops(false)
            .expectedNodeCount(container.size)
            .build<Code>()

        container.forEach { (_, code) ->
            populateGraph(mutableGraph, code)
        }

        ImmutableGraph.copyOf(mutableGraph)
    }

    @Suppress("UnstableApiUsage")
    private fun populateGraph(
        graph: MutableGraph<Code>,
        code: Code
    ) {
        // Make sure nodes without dependencies are in the graph
        graph.addNode(code)

        code.dependencies.forEach {
            // Don't recursive if the edge was already in the graph
            if (graph.putEdge(code, it)) {
                require(!hasCircuits(graph)) {
                    "Adding an edge between $code and $it caused a circuit."
                }

                it.dependencies.forEach {
                    populateGraph(graph, it)
                }
            }
        }
    }

    private fun hasCircuits(graph: MutableGraph<Code>): Boolean {
        val visited = mutableSetOf<Code>()

        graph.nodes().forEach { node ->
            val reachable = bfs(graph, node)

            if (visited anyIn reachable) {
                return true
            }

            visited += node
        }

        return false
    }

    private fun bfs(graph: MutableGraph<Code>, root: Code): Set<Code> {
        val visited = mutableSetOf<Code>()
        val queue = mutableListOf<Code>()

        visited += root
        queue += root

        while (queue.isNotEmpty()) {
            val node = queue.first()
            queue.remove(node)

            graph.successors(node).forEach {
                if (it !in visited) {
                    visited += it
                    queue += it
                }
            }
        }

        return visited
    }
}

private infix fun <E> Iterable<E>.anyIn(other: Iterable<E>) = any { it in other }
