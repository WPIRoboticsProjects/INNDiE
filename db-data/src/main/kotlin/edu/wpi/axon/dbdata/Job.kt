package edu.wpi.axon.dbdata

import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.datasetModule
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.loss.lossModule
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.tfdata.optimizer.optimizerModule
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.plus

@Serializable
data class Job(
    val name: String,
    @Polymorphic val status: TrainingScriptProgress,
    val userOldModelPath: String,
    val userNewModelName: String,
    @Polymorphic val userDataset: Dataset,
    @Polymorphic val userOptimizer: Optimizer,
    @Polymorphic val userLoss: Loss,
    val userMetrics: Set<String>,
    val userEpochs: Int,
    val generateDebugComments: Boolean,
    val id: Int
) {

    fun serialize(): String = Json(
        JsonConfiguration.Stable,
        context = datasetModule + optimizerModule + lossModule + trainingScriptProgressModule
    ).stringify(serializer(), this)

    companion object {

        fun deserialize(data: String): Job = Json(
            JsonConfiguration.Stable,
            context = datasetModule + optimizerModule + lossModule + trainingScriptProgressModule
        ).parse(serializer(), data)
    }
}
