@file:Suppress("UnstableApiUsage")

package edu.wpi.axon.tfdata

import com.google.common.graph.ImmutableGraph
import edu.wpi.axon.tfdata.layer.Layer
import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

typealias LayerGraph = ImmutableGraph<Layer.MetaLayer>

@Serializable
sealed class Model {

    /**
     * The name of the model.
     */
    abstract val name: String

    /**
     * A linear stack of layers.
     */
    @Serializable
    data class Sequential(
        override val name: String,
        val batchInputShape: List<Int?>,
        val layers: Set<Layer.MetaLayer>
    ) : Model()

    @Serializable
    data class General(
        override val name: String,
        val input: Set<InputData>,
        @ContextualSerialization val layers: LayerGraph, // TODO: Fix this serializer
        val output: Set<OutputData>
    ) : Model() {

        @Serializable
        data class InputData(
            val id: String,
            val type: List<Int?>,
            val batchSize: Int? = null,
            val dtype: Double? = null,
            val sparse: Boolean = false
        )

        @Serializable
        data class OutputData(val id: String)
    }

    fun serialize(): String = Json(
        JsonConfiguration.Stable
    ).stringify(serializer(), this)

    companion object {
        fun deserialize(data: String) = Json(
            JsonConfiguration.Stable
        ).parse(serializer(), data)
    }
}
