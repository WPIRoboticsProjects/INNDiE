package edu.wpi.axon.dbdata

import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.util.FilePath
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

/**
 * @param userOldModelPath The path to the model to load.
 * @param userNewModelName The name of the model to save to.
 * @param userDataset The dataset to train on.
 * @param userOptimizer The [Optimizer] to use.
 * @param userLoss The [Loss] function to use.
 * @param userMetrics Any metrics.
 * @param userEpochs The number of epochs.
 * @param userNewModel The new model configuration (the old model after it was configured by the
 * user).
 * @param generateDebugComments Whether to put debug comments in the output.
 * @param id The database-generated unique id. Do not modify.
 */
@Serializable
data class Job(
    var name: String,
    var status: TrainingScriptProgress,
    var userOldModelPath: FilePath,
    var userNewModelName: FilePath,
    var userDataset: Dataset,
    var userOptimizer: Optimizer,
    var userLoss: Loss,
    var userMetrics: Set<String>,
    var userEpochs: Int,
    var userNewModel: Model,
    var generateDebugComments: Boolean,
    var id: Int = -1
) {

    fun serialize(): String = Json(
        JsonConfiguration.Stable
    ).stringify(serializer(), this)

    companion object {

        fun deserialize(data: String): Job = Json(
            JsonConfiguration.Stable
        ).parse(serializer(), data)
    }
}
