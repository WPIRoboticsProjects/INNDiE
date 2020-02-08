package edu.wpi.axon.training

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

/**
 * All the ways a model can be deployed. These affect how the model is trained.
 */
@Serializable
sealed class ModelDeploymentTarget {

    /**
     * The model is going to be deployed to a desktop-class machine that can load the standard
     * HDF5 file output.
     */
    @Serializable
    object Desktop : ModelDeploymentTarget()

    /**
     * The model is going to be deployed to a mobile system that uses the Coral Edge TPU.
     *
     * // TODO: Rename this to representativeDatasetRatio because it's not really a percentage
     * @param representativeDatasetPercentage The percentage of the training dataset to use for the
     * representative dataset used for post-training quantization.
     */
    @Serializable
    data class Coral(val representativeDatasetPercentage: Double) : ModelDeploymentTarget()

    fun serialize(): String = Json(
        JsonConfiguration.Stable
    ).stringify(serializer(), this)

    companion object {
        fun deserialize(data: String) = Json(
            JsonConfiguration.Stable
        ).parse(serializer(), data)
    }
}
