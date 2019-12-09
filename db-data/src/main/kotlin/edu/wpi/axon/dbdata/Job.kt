package edu.wpi.axon.dbdata

import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

@Serializable
data class Job(
    var name: String,
    var status: TrainingScriptProgress,
    var userOldModelPath: String,
    var userNewModelName: String,
    var userDataset: Dataset,
    var userOptimizer: Optimizer,
    var userLoss: Loss,
    var userMetrics: Set<String>,
    var userEpochs: Int,
    var userModel: Model,
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
