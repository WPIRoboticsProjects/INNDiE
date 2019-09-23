@file:Suppress("UnstableApiUsage")

package edu.wpi.axon.tflayerloader

import arrow.core.Either
import arrow.core.None
import arrow.core.Some
import arrow.core.extensions.either.monad.binding
import com.google.common.graph.GraphBuilder
import com.google.common.graph.ImmutableGraph
import edu.wpi.axon.tfdata.layer.SealedLayer
import edu.wpi.axon.util.checkIslands

class DefaultLayersToGraph : LayersToGraph {

    override fun convertToGraph(
        layers: Set<SealedLayer.MetaLayer>
    ): Either<String, ImmutableGraph<SealedLayer.MetaLayer>> {
        val graph = GraphBuilder.directed()
            .allowsSelfLoops(false)
            .expectedNodeCount(layers.size)
            .build<SealedLayer.MetaLayer>()

        return binding {
            layers.forEach { layer ->
                graph.addNode(layer)
            }

            layers.forEach { layer ->
                val inboundNodes: Set<SealedLayer.MetaLayer> =
                    when (val inputs = layer.layer.inputs) {
                        is Some -> graph.nodes().filterTo(mutableSetOf()) { it.name in inputs.t }
                        is None -> emptySet()
                    }

                inboundNodes.forEach { inboundNode ->
                    graph.putEdge(inboundNode, layer)
                }
            }

            graph.checkIslands().bind()
            ImmutableGraph.copyOf(graph)
        }
    }
}
