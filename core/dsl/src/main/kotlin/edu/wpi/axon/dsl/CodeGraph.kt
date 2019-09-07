package edu.wpi.axon.dsl

import arrow.Kind2
import arrow.core.Either
import arrow.core.ForEither
import arrow.core.Left
import arrow.core.Right
import arrow.core.extensions.either.monad.binding
import arrow.data.ListK
import arrow.data.extensions.listk.applicative.applicative
import arrow.data.fix
import arrow.data.k
import com.google.common.graph.GraphBuilder
import com.google.common.graph.ImmutableGraph
import com.google.common.graph.MutableGraph
import edu.wpi.axon.dsl.container.PolymorphicNamedDomainObjectContainer

/**
 * Parses a [PolymorphicNamedDomainObjectContainer] of [Code] into an [ImmutableGraph], assuring
 * that all [Code] nodes are unique and dependencies are properly ordered. Nodes on separate
 * branches of the graph will not be deduplicated to preserve those dependencies. For example,
 * this is a possible output graph:
 * +---+      +---+
 * | A +----->+ B +--------+
 * +---+      +---+        v
 *                       +-+-+
 *                       | D |
 *                       +-+-+
 * +---+      +---+        ^
 * | A +----->+ C +--------+
 * +---+      +---+
 */
@Suppress("UnstableApiUsage")
class CodeGraph(
    private val container: PolymorphicNamedDomainObjectContainer<AnyCode>
) {

    val graph: Either<String, ImmutableGraph<AnyCode>> by lazy {
        val mutableGraph = GraphBuilder.directed()
            .allowsSelfLoops(false)
            .expectedNodeCount(container.size)
            .build<AnyCode>()

        binding<String, ImmutableGraph<AnyCode>> {
            container.forEach { (_, code) ->
                populateGraphUsingDependencies(mutableGraph, code).bind()
            }

            populateGraphUsingVariables(mutableGraph, container).bind()

            checkIslands(mutableGraph).bind()
            ImmutableGraph.copyOf(mutableGraph)
        }
    }

    private fun checkIslands(mutableGraph: MutableGraph<AnyCode>): Kind2<ForEither, String, Unit> {
        val allNodes = mutableGraph.nodes()
        if (allNodes.isEmpty()) {
            // Can't have islands with an empty graph.
            return Right(Unit)
        }

        // BFS should find every node if there is only one island. Starting node does not matter.
        // Supply a custom nextNode function because the direction BFS moves in is irrelevant,
        // it just has to touch every node in an island.
        val reachable = bfs(mutableGraph, allNodes.first()) { successors(it) + predecessors(it) }
        return if (reachable == allNodes) {
            Right(Unit)
        } else {
            Left(
                """
                |The following nodes are not reachable:
                |${(allNodes - reachable).joinToString()}
                """.trimMargin()
            )
        }
    }

    /**
     * Add nodes for codes and edges between codes that are linked with [Code.dependencies].
     */
    private fun populateGraphUsingDependencies(
        graph: MutableGraph<AnyCode>,
        code: AnyCode
    ): Kind2<ForEither, String, Unit> {
        // Make sure nodes without dependencies are in the graph
        graph.addNode(code)

        code.dependencies.forEach {
            // Don't recurse if the edge was already in the graph
            if (graph.putEdge(it, code)) {
                if (hasCircuits(graph, code)) {
                    return Left("Adding an edge from $it to $code caused a circuit.")
                }

                it.dependencies.forEach {
                    populateGraphUsingDependencies(graph, it)
                }
            }
        }

        return Right(Unit)
    }

    /**
     * Add edges between pairs of codes where one code's [Code.outputs] is the other code's
     * [Code.inputs].
     */
    private fun populateGraphUsingVariables(
        graph: MutableGraph<AnyCode>,
        container: PolymorphicNamedDomainObjectContainer<AnyCode>
    ): Either<String, Unit> {
        val codesK = container.values.toList().k()
        ListK.applicative()
            .tupled(codesK, codesK)
            .fix()
            .filter { (taskA, taskB) ->
                taskA.outputs anyIn taskB.inputs
            }
            .forEach { (codeWithOutputs, codeWithInputs) ->
                graph.putEdge(codeWithOutputs, codeWithInputs)
                if (hasCircuits(graph, codeWithInputs)) {
                    return Left(
                        "Adding an edge between $codeWithOutputs and " +
                            "$codeWithInputs caused a circuit."
                    )
                }
            }

        return Right(Unit)
    }

    private fun hasCircuits(
        graph: MutableGraph<AnyCode>,
        root: AnyCode,
        visited: Set<AnyCode> = emptySet()
    ): Boolean {
        graph.successors(root).forEach { node ->
            val reachable = bfs(graph, node)

            // Finding an already visited node in the new reachable set implies a circuit. The
            // reachable set will strictly decrease for successor nodes in a DAG.
            if (visited anyIn reachable || hasCircuits(graph, node, visited + node)) {
                return true
            }
        }

        return false
    }

    private fun bfs(
        graph: MutableGraph<AnyCode>,
        root: AnyCode,
        nextNodes: MutableGraph<AnyCode>.(AnyCode) -> Set<AnyCode> =
            MutableGraph<AnyCode>::successors
    ): Set<AnyCode> {
        val visited = mutableSetOf<AnyCode>()
        val queue = mutableListOf<AnyCode>()

        visited += root
        queue += root

        while (queue.isNotEmpty()) {
            val node = queue.first()
            queue.remove(node)

            graph.nextNodes(node).forEach {
                if (it !in visited) {
                    visited += it
                    queue += it
                }
            }
        }

        return visited
    }
}
