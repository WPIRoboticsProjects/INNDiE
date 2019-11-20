package edu.wpi.axon.dbdata

import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.plus

@Serializable
data class Job(
    val name: String,
    val status: TrainingScriptProgress,
    val userOldModelPath: String,
    val userNewModelName: String,
    val userDataset: Dataset,
    val userOptimizer: Optimizer,
    val userLoss: Loss,
    val userMetrics: Set<String>,
    val userEpochs: Int,
    val generateDebugComments: Boolean,
    val id: Int = -1
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
