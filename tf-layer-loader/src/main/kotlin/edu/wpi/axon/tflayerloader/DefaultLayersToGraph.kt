@file:Suppress("UnstableApiUsage")

package edu.wpi.axon.tflayerloader

import arrow.core.Either
import arrow.core.None
import arrow.core.Some
import arrow.core.extensions.either.monadError.monadError
import arrow.core.extensions.fx
import arrow.fx.IO
import com.google.common.base.Throwables
import com.google.common.graph.GraphBuilder
import com.google.common.graph.ImmutableGraph
import edu.wpi.axon.tfdata.LayerGraph
import edu.wpi.axon.tfdata.layer.SealedLayer
import edu.wpi.axon.util.checkIslands

class DefaultLayersToGraph : LayersToGraph {

    override fun convertToGraph(
        layers: Set<SealedLayer.MetaLayer>
    ): Either<String, LayerGraph> {
        val graph = GraphBuilder.directed()
            .allowsSelfLoops(false)
            .expectedNodeCount(layers.size)
            .build<SealedLayer.MetaLayer>()

        return Either.fx {
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
                    !IO {
                        graph.putEdge(inboundNode, layer)
                    }.attempt().unsafeRunSync().mapLeft { Throwables.getStackTraceAsString(it) }
                }
            }

            !graph.checkIslands()
            !Either.monadError<String>().layerGraphIsValid(graph)
            ImmutableGraph.copyOf(graph)
        }
    }
}