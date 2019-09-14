package edu.wpi.axon.tflayerloader

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Layer
import edu.wpi.axon.tfdata.layer.SealedLayer
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
                parseMetaLayer(className, layerData)
            }
        }
    }

    private fun parseMetaLayer(name: String, json: JsonObject): SealedLayer.MetaLayer =
        when (json["trainable"] as Boolean) {
            true -> SealedLayer.MetaLayer.TrainableLayer(
                json["name"] as String,
                parseLayer(name, json)
            )

            false -> SealedLayer.MetaLayer.UntrainableLayer(
                json["name"] as String,
                parseLayer(name, json)
            )
        }

    private fun parseLayer(name: String, json: JsonObject): Layer = when (name) {
        "Dense" -> SealedLayer.Dense(
            json["name"] as String,
            json["units"] as Int,
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
