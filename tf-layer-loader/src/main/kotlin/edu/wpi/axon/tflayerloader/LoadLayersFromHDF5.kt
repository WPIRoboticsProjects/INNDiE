package edu.wpi.axon.tflayerloader

import arrow.core.Tuple2
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
class LoadLayersFromHDF5 {

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

    private fun parseModel(json: JsonObject): Model {
        val config = json["config"] as JsonObject
        val name = config["name"] as String

        var batchInputShape: List<Int?> by singleAssign()

        @Suppress("UNCHECKED_CAST")
        val layers = (config["layers"] as JsonArray<JsonObject>).map {
            val className = it["class_name"] as String
            val layerData = it["config"] as JsonObject

            (layerData["batch_input_shape"] as JsonArray<Int?>?)?.let {
                batchInputShape = it
            }

            val layer = parseLayer(className, layerData)
            parseMetaLayer(layer, layerData)
        }

        return when (json["class_name"] as String) {
            "Sequential" -> Model.Sequential(
                name,
                batchInputShape,
                layers.toSet()
            )

            else -> Model.Unknown(
                name,
                batchInputShape,
                layers.toSet()
            )
        }
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
    private fun parseLayer(className: String, json: JsonObject): SealedLayer = when (className) {
        "Dense" -> SealedLayer.Dense(
            json["name"] as String,
            json["units"] as Int,
            parseActivation(json)
        )

        "Conv2D" -> SealedLayer.Conv2D(
            json["name"] as String,
            json["filters"] as Int,
            (json["kernel_size"] as JsonArray<Int>).let { Tuple2(it[0], it[1]) },
            parseActivation(json)
        )

        "InputLayer" -> SealedLayer.InputLayer(
            json["name"] as String,
            (json["batch_input_shape"] as JsonArray<Int?>).toList()
        )

        else -> SealedLayer.UnknownLayer(
            json["name"] as String
        )
    }

    private fun parseActivation(json: JsonObject): Activation =
        when (val name = json["activation"] as String) {
            "relu" -> Activation.ReLu
            "softmax" -> Activation.SoftMax
            else -> Activation.UnknownActivation(name)
        }
}
