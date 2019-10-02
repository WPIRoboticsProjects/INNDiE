@file:Suppress("UNCHECKED_CAST")

package edu.wpi.axon.tflayerloader

import arrow.core.Either
import arrow.core.Left
import arrow.core.None
import arrow.core.Option
import arrow.core.Right
import arrow.core.Tuple2
import arrow.core.some
import arrow.fx.IO
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.PoolingDataFormat
import edu.wpi.axon.tfdata.layer.PoolingPadding
import edu.wpi.axon.tfdata.layer.SealedLayer
import edu.wpi.axon.util.singleAssign
import io.jhdf.HdfFile
import java.io.File

/**
 * Loads TensorFlow layers from an HDF5 file.
 */
class LoadLayersFromHDF5(
    private val layersToGraph: LayersToGraph
) {

    /**
     * Load layers from the [file].
     *
     * @param file The file to load from.
     * @return The layers in the file.
     */
    fun load(file: File): IO<Model> = IO {
        HdfFile(file).use {
            val config = it.getAttribute("model_config").data as String
            val data = Parser.default().parse(config.byteInputStream()) as JsonObject
            parseModel(data)
        }
    }

    private fun parseModel(json: JsonObject): Model =
        when (val className = json["class_name"] as String) {
            "Sequential" -> parseSequentialModel(json)
            "Model" -> parseGeneralModel(json)
            else -> throw IllegalStateException("Unknown model class $className")
        }

    private fun parseSequentialModel(json: JsonObject): Model.Sequential {
        val config = json["config"] as JsonObject

        var batchInputShape: List<Int?> by singleAssign()

        val layers = (config["layers"] as JsonArray<JsonObject>).mapTo(mutableSetOf()) {
            val layer = parseLayer(it["class_name"] as String, it)

            ((it["config"] as JsonObject)["batch_input_shape"] as JsonArray<Int?>?)?.let {
                batchInputShape = it.toList()
            }

            parseMetaLayer(layer, it["config"] as JsonObject)
        }

        return Model.Sequential(
            config["name"] as String,
            batchInputShape,
            layers.toSet()
        )
    }

    private fun parseGeneralModel(json: JsonObject): Model.General {
        val config = json["config"] as JsonObject

        val inputLayerIds = (config["input_layers"] as JsonArray<JsonArray<Any>>)
            .mapTo(mutableSetOf()) {
                it.first() as String
            }

        val outputLayerIds = (config["output_layers"] as JsonArray<JsonArray<Any>>)
            .mapTo(mutableSetOf()) {
                it.first() as String
            }

        val layers = (config["layers"] as JsonArray<JsonObject>).mapTo(mutableSetOf()) {
            val layer = parseLayer(it["class_name"] as String, it)
            parseMetaLayer(layer, it["config"] as JsonObject)
        }

        return Model.General(
            name = config["name"] as String,
            input = inputLayerIds.mapTo(mutableSetOf()) { inputId ->
                Model.General.InputData(
                    inputId,
                    (layers.first {
                        it.name == inputId &&
                            it is SealedLayer.MetaLayer.UntrainableLayer &&
                            it.layer is SealedLayer.InputLayer
                    }.layer as SealedLayer.InputLayer).batchInputShape
                )
            },
            layers = layersToGraph.convertToGraph(layers).fold({ TODO() }, { it }),
            output = outputLayerIds.mapTo(mutableSetOf()) { Model.General.OutputData(it) }
        )
    }

    private fun parseMetaLayer(layer: SealedLayer, json: JsonObject): SealedLayer.MetaLayer {
        return when (layer) {
            // Don't wrap a MetaLayer more than once
            is SealedLayer.MetaLayer -> layer

            else -> {
                val name = json["name"] as String
                when (val trainable = json["trainable"] as Boolean?) {
                    null -> SealedLayer.MetaLayer.UntrainableLayer(name, layer)
                    else -> SealedLayer.MetaLayer.TrainableLayer(name, layer, trainable)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseLayer(className: String, data: JsonObject): SealedLayer {
        val json = data["config"] as JsonObject
        val name = json["name"] as String
        return when (className) {
            "Dense" -> SealedLayer.Dense(
                name,
                data.inboundNodes(),
                json["units"] as Int,
                parseActivation(json)
            )

            "Conv2D" -> SealedLayer.Conv2D(
                name,
                data.inboundNodes(),
                json["filters"] as Int,
                (json["kernel_size"] as JsonArray<Int>).let { Tuple2(it[0], it[1]) },
                parseActivation(json)
            )

            "InputLayer" -> SealedLayer.InputLayer(
                name,
                (json["batch_input_shape"] as JsonArray<Int?>).toList().let {
                    require(it.first() == null) {
                        "First element of InputLayer batch_input_shape was not null: " +
                            it.joinToString()
                    }
                    it.drop(1)
                }
            )

            "Dropout" -> SealedLayer.Dropout(
                name,
                data.inboundNodes(),
                json["rate"] as Double,
                (json["noise_shape"] as JsonArray<Int>?)?.toList()?.let {
                    throw IllegalStateException(
                        "noise_shape was not null (this isn't bad): ${it.joinToString()}"
                    )
                },
                json["seed"] as Int?
            )

            "MaxPool2D", "MaxPooling2D" -> SealedLayer.MaxPooling2D(
                name,
                data.inboundNodes(),
                json["pool_size"].tuple2OrInt(),
                json["strides"].tuple2OrIntOrNull(),
                json["padding"].poolingPadding(),
                json["data_format"].poolingDataFormatOrNull()
            )

            else -> SealedLayer.UnknownLayer(
                name,
                data.inboundNodes()
            )
        }
    }

    private fun parseActivation(json: JsonObject): Activation =
        when (val name = json["activation"] as String) {
            "relu" -> Activation.ReLu
            "softmax" -> Activation.SoftMax
            else -> Activation.UnknownActivation(name)
        }
}

private fun Any?.poolingPadding(): PoolingPadding = when (this as? String) {
    "valid" -> PoolingPadding.Valid
    "same" -> PoolingPadding.Same
    else -> throw IllegalArgumentException("Not convertible: $this")
}

private fun Any?.poolingDataFormatOrNull(): PoolingDataFormat? = when (this as? String) {
    "channels_first" -> PoolingDataFormat.ChannelsFirst
    "channels_last" -> PoolingDataFormat.ChannelsLast
    null -> null
    else -> throw IllegalArgumentException("Not convertible: $this")
}

private fun Any?.tuple2OrInt(): Either<Int, Tuple2<Int, Int>> = when {
    this is Int -> Left(this)

    this as? JsonArray<Int> != null -> {
        require(this.size == 2)
        Right(Tuple2(this[0], this[1]))
    }

    else -> throw IllegalArgumentException("Not convertible: $this")
}

private fun Any?.tuple2OrIntOrNull(): Either<Int, Tuple2<Int, Int>>? = when {
    this is Int -> Left(this)

    this as? JsonArray<Int> != null -> {
        require(this.size == 2)
        Right(Tuple2(this[0], this[1]))
    }

    else -> if (this == null) {
        null
    } else {
        throw IllegalArgumentException("Not convertible: $this")
    }
}

@Suppress("UNCHECKED_CAST")
private fun JsonObject.inboundNodes(): Option<Set<String>> {
    // None is valid for Sequential models
    val inboundNodes = this["inbound_nodes"] ?: return None
    inboundNodes as JsonArray<JsonArray<JsonArray<Any>>>
    require(inboundNodes.size == 1)
    return inboundNodes[0].mapTo(mutableSetOf()) {
        it[0] as String
    }.some()
}
