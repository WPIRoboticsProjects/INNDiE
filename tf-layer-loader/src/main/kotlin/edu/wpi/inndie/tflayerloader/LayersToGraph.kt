package edu.wpi.inndie.tflayerloader

import arrow.core.Either
import edu.wpi.inndie.tfdata.LayerGraph
import edu.wpi.inndie.tfdata.layer.Layer

@Suppress("UnstableApiUsage")
interface LayersToGraph {

    /**
     * Convert the [layers] into their graph form. Only works for non-Sequential layers.
     *
     * @param layers The layers to convert.
     * @return The graph representation of the layers.
     */
    fun convertToGraph(layers: Set<Layer.MetaLayer>): Either<String, LayerGraph>
}
