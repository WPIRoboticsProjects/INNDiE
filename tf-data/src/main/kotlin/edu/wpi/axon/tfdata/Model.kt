@file:Suppress("UnstableApiUsage")

package edu.wpi.axon.tfdata

import com.google.common.graph.ImmutableGraph
import edu.wpi.axon.tfdata.layer.SealedLayer

typealias LayerGraph = ImmutableGraph<SealedLayer.MetaLayer>

sealed class Model {

    /**
     * The name of the model.
     */
    abstract val name: String

    /**
     * A linear stack of layers.
     */
    data class Sequential(
        override val name: String,
        val batchInputShape: List<Int?>,
        val layers: Set<SealedLayer.MetaLayer>
    ) : Model()

    data class General(
        override val name: String,
        val input: Set<InputData>,
        val layers: LayerGraph,
        val output: Set<OutputData>
    ) : Model() {

        data class InputData(
            val id: String,
            val type: List<Int?>,
            val batchSize: Int? = null,
            val dtype: Number? = null,
            val sparse: Boolean = false
        )

        data class OutputData(val id: String)
    }
}
