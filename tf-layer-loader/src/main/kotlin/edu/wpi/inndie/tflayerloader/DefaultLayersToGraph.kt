@file:Suppress("UnstableApiUsage")

package edu.wpi.inndie.tflayerloader

import arrow.core.Either
import arrow.core.extensions.either.monadError.monadError
import arrow.core.extensions.fx
import arrow.fx.IO
import com.google.common.base.Throwables
import com.google.common.graph.GraphBuilder
import com.google.common.graph.ImmutableGraph
import edu.wpi.inndie.tfdata.LayerGraph
import edu.wpi.inndie.tfdata.layer.Layer
import edu.wpi.inndie.util.checkIslands

class DefaultLayersToGraph : LayersToGraph {

    override fun convertToGraph(
        layers: Set<Layer.MetaLayer>
    ): Either<String, LayerGraph> {
        val graph = GraphBuilder.directed()
            .allowsSelfLoops(false)
            .expectedNodeCount(layers.size)
            .build<Layer.MetaLayer>()

        return Either.fx {
            layers.forEach { layer ->
                graph.addNode(layer)
            }

            layers.forEach { layer ->
                val inboundNodes: Set<Layer.MetaLayer> =
                    when (val inputs = layer.layer.inputs) {
                        null -> emptySet()
                        else -> graph.nodes().filterTo(mutableSetOf()) { it.name in inputs }
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
