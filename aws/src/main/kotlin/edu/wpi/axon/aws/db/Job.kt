package edu.wpi.axon.aws.db

import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer

// TODO: What else does a job have?
data class Job(
    val name: String,
    val userOldModelPath: String,
    val userNewModelName: String,
    val userDataset: Dataset,
    val userOptimizer: Optimizer,
    val userLoss: Loss,
    val userMetrics: Set<String>,
    val userEpochs: Int,
    val generateDebugComments: Boolean
) {

    fun serialize(): String = TODO()

    companion object {

        fun deserialize(data: String): Job = TODO()
    }
}
