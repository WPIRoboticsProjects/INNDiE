package edu.wpi.axon.dsl

import arrow.Kind2
import arrow.core.Either
import arrow.core.ForEither
import arrow.core.Left
import arrow.core.ListK
import arrow.core.Right
import arrow.core.extensions.fx
import arrow.core.extensions.listk.applicative.applicative
import arrow.core.fix
import arrow.core.k
import com.google.common.graph.GraphBuilder
import com.google.common.graph.ImmutableGraph
import com.google.common.graph.MutableGraph
import edu.wpi.axon.dsl.container.PolymorphicNamedDomainObjectContainer
import edu.wpi.axon.util.anyIn
import edu.wpi.axon.util.checkIslands
import edu.wpi.axon.util.hasCircuits

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

        Either.fx<String, ImmutableGraph<AnyCode>> {
            container.forEach { (_, code) ->
                populateGraphUsingDependencies(mutableGraph, code).bind()
            }

            populateGraphUsingVariables(mutableGraph, container).bind()

            mutableGraph.checkIslands().bind()
            ImmutableGraph.copyOf(mutableGraph)
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
                if (graph.hasCircuits(code)) {
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
            .filter { (taskA, taskB) -> taskA.outputs anyIn taskB.inputs }
            .forEach { (codeWithOutputs, codeWithInputs) ->
                graph.putEdge(codeWithOutputs, codeWithInputs)
                if (graph.hasCircuits(codeWithInputs)) {
                    return Left(
                        "Adding an edge between $codeWithOutputs and " +
                            "$codeWithInputs caused a circuit."
                    )
                }
            }

        return Right(Unit)
    }
}
