package edu.wpi.axon.tflayerloader

import arrow.core.Either
import com.google.common.graph.ImmutableGraph
import edu.wpi.axon.tfdata.layer.SealedLayer

@Suppress("UnstableApiUsage")
interface LayersToGraph {

    fun convertToGraph(
        layers: Set<SealedLayer.MetaLayer>
    ): Either<String, ImmutableGraph<SealedLayer.MetaLayer>>
}
