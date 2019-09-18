package edu.wpi.axon.tflayerloader

import arrow.core.Tuple2
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Layer
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
    fun load(file: File): Model {
        HdfFile(file).use {
            val config = it.getAttribute("model_config").data as String
            val data = Parser.default().parse(config.byteInputStream()) as JsonObject
            return parseModel(data)
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

            parseMetaLayer(className, layerData)
        }

        return when (json["class_name"] as String) {
            "Sequential" -> Model.Sequential(
                name,
                batchInputShape,
                layers.toSet()
            )

            else -> Model.Sequential(
                name,
                batchInputShape,
                layers.toSet()
            )
        }
    }

    private fun parseMetaLayer(name: String, json: JsonObject): SealedLayer.MetaLayer =
        when (val trainable = json["trainable"] as Boolean?) {
            null -> SealedLayer.MetaLayer.UntrainableLayer(
                json["name"] as String,
                parseLayer(name, json)
            )

            else -> SealedLayer.MetaLayer.TrainableLayer(
                json["name"] as String,
                parseLayer(name, json),
                trainable
            )
        }

    @Suppress("UNCHECKED_CAST")
    private fun parseLayer(name: String, json: JsonObject): Layer = when (name) {
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
