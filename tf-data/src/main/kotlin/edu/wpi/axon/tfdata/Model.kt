@file:Suppress("UnstableApiUsage")

package edu.wpi.axon.tfdata

import com.google.common.graph.ImmutableGraph
import edu.wpi.axon.tfdata.layer.SealedLayer

typealias LayerGraph = ImmutableGraph<SealedLayer.MetaLayer>

sealed class Model(
    open val name: String
) {

    /**
     * A linear stack of layers.
     */
    data class Sequential(
        override val name: String,
        val batchInputShape: List<Int?>,
        val layers: Set<SealedLayer.MetaLayer>
    ) : Model(name)

    data class General(
        override val name: String,
        val input: Set<InputData>,
        val layers: LayerGraph,
        val output: Set<OutputData>
    ) : Model(name) {
        data class InputData(val id: String, val type: List<Int?>)
        data class OutputData(val id: String)
    }
}
