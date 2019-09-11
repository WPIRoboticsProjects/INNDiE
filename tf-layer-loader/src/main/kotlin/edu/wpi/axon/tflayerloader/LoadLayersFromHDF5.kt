package edu.wpi.axon.tflayerloader

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import edu.wpi.axon.tflayers.Activation
import edu.wpi.axon.tflayers.Layer
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
    fun load(file: File): List<Layer> {
        HdfFile(file).use {
            val config = it.getAttribute("model_config").data as String
            val data = Parser.default().parse(config.byteInputStream()) as JsonObject

            @Suppress("UNCHECKED_CAST")
            val layers = (data["config"] as JsonObject)["layers"] as JsonArray<JsonObject>

            return layers.map {
                val className = it["class_name"] as String
                val layerData = it["config"] as JsonObject
                parseLayer(className, layerData)
            }
        }
    }

    private fun parseLayer(name: String, json: JsonObject): Layer = when (name) {
        "Dense" -> Layer.Dense(
            json["name"] as String,
            json["trainable"] as Boolean,
            json["units"] as Int,
            parseActivation(json)
        )

        else -> Layer.UnknownLayer(
            json["name"] as String,
            json["trainable"] as Boolean
        )
    }

    private fun parseActivation(json: JsonObject): Activation =
        when (val name = json["activation"] as String) {
            "relu" -> Activation.ReLu
            "softmax" -> Activation.SoftMax
            else -> Activation.UnknownActivation(name)
        }
}
