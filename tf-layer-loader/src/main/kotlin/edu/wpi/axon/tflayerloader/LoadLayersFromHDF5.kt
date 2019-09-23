@file:Suppress("UNCHECKED_CAST")

package edu.wpi.axon.tflayerloader

import arrow.core.None
import arrow.core.Option
import arrow.core.Tuple2
import arrow.core.some
import arrow.effects.IO
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.layer.Activation
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
            layers = layersToGraph.convertToGraph(layers.filterTo(mutableSetOf()) {
                it.layer !is SealedLayer.InputLayer
            }).fold({ TODO() }, { it }),
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
        return when (className) {
            "Dense" -> SealedLayer.Dense(
                json["name"] as String,
                data.inboundNodes(),
                json["units"] as Int,
                parseActivation(json)
            )

            "Conv2D" -> SealedLayer.Conv2D(
                json["name"] as String,
                data.inboundNodes(),
                json["filters"] as Int,
                (json["kernel_size"] as JsonArray<Int>).let { Tuple2(it[0], it[1]) },
                parseActivation(json)
            )

            "InputLayer" -> SealedLayer.InputLayer(
                json["name"] as String,
                (json["batch_input_shape"] as JsonArray<Int?>).toList()
            )

            else -> SealedLayer.UnknownLayer(
                json["name"] as String,
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

@Suppress("UNCHECKED_CAST")
private fun JsonObject.inboundNodes(): Option<Set<String>> {
    val inboundNodes = this["inbound_nodes"] ?: return None
    inboundNodes as JsonArray<JsonArray<JsonArray<Any>>>
    require(inboundNodes.size == 1)
    return inboundNodes[0].mapTo(mutableSetOf()) {
        it[0] as String
    }.some()
}
