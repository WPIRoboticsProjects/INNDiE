package edu.wpi.axon.tflayerloader

import arrow.core.Either
import edu.wpi.axon.tfdata.LayerGraph
import edu.wpi.axon.tfdata.layer.SealedLayer

@Suppress("UnstableApiUsage")
interface LayersToGraph {

    /**
     * Convert the [layers] into their graph form. Only works for non-Sequential layers.
     *
     * @param layers The layers to convert.
     * @return The graph representation of the layers.
     */
    fun convertToGraph(layers: Set<SealedLayer.MetaLayer>): Either<String, LayerGraph>
}
