@file:Suppress("UnstableApiUsage")

package edu.wpi.axon.tflayerloader

import arrow.Kind
import arrow.core.None
import arrow.core.Some
import arrow.typeclasses.MonadError
import com.google.common.graph.Graph
import edu.wpi.axon.tfdata.layer.SealedLayer
import edu.wpi.axon.util.allIn
import edu.wpi.axon.util.breadthFirstSearch

/**
 * Validated a layer graph.
 *
 * @param layerGraph The layer graph to validate.
 * @return No error if the graph is valid.
 */
fun <F> MonadError<F, String>.layerGraphIsValid(
    layerGraph: Graph<SealedLayer.MetaLayer>
): Kind<F, Unit> =
    fx.monad {
        layerNamesAreUnique(layerGraph).bind()
        layerGraph.nodes().forEach {
            hasInputs(it).bind()
            inputsAreDeclared(layerGraph, it).bind()
        }
    }

/**
 * @param layerGraph The layer graph.
 * @return No error if all the layers' names are unique.
 */
private fun <F> MonadError<F, String>.layerNamesAreUnique(layerGraph: Graph<SealedLayer.MetaLayer>) =
    layerGraph.nodes().let { nodes ->
        if (nodes.mapTo(mutableSetOf()) { it.name }.size == nodes.size) {
            just(Unit)
        } else {
            raiseError("Not all the layer names are unique: ${nodes.joinToString("\n")}")
        }
    }

/**
 * A layer must have inputs (unless it's a [SealedLayer.InputLayer], which can't have inputs).
 *
 * @param layer The layer.
 * @return No error if the layer has inputs.
 */
private fun <F> MonadError<F, String>.hasInputs(layer: SealedLayer.MetaLayer) =
    if (layer.inputs is Some || layer.layer is SealedLayer.InputLayer) {
        just(Unit)
    } else {
        raiseError("The layer does not have inputs: $layer")
    }

/**
 * A layer's inputs must already be in the layer graph by the time they are used.
 *
 * @param layerGraph The layer graph.
 * @param layer The layer to check.
 * @return No error if all inputs are already declared.
 */
private fun <F> MonadError<F, String>.inputsAreDeclared(
    layerGraph: Graph<SealedLayer.MetaLayer>,
    layer: SealedLayer.MetaLayer
): Kind<F, Unit> = when (val inputs = layer.inputs) {
    is None -> just(Unit)
    is Some -> {
        val predecessorLayers =
            layerGraph.breadthFirstSearch(layer, Graph<SealedLayer.MetaLayer>::predecessors)
                .map { it.name }

        if (inputs.t allIn predecessorLayers) {
            just(Unit)
        } else {
            raiseError(
                "Not all of the layer's inputs are declared previously: " +
                    (inputs.t subtract predecessorLayers).joinToString()
            )
        }
    }
}
