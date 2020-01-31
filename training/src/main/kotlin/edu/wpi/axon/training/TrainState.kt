package edu.wpi.axon.training

import arrow.core.Option
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.util.FilePath
import edu.wpi.axon.util.allS3OrLocal

/**
 * All configuration data needed to generate a training script.
 *
 * @param userOldModelPath The path to the model to load.
 * @param userNewModelPath The name of the model to save to.
 * @param userDataset The dataset to train on.
 * @param userOptimizer The [Optimizer] to use.
 * @param userLoss The [Loss] function to use.
 * @param userMetrics Any metrics.
 * @param userEpochs The number of epochs.
 * @param userNewModel The new model.
 * @param generateDebugComments Whether to put debug comments in the output.
 * @param target Where the model will be deployed.
 * @param datasetPlugin The plugin used to process the dataset after it is loaded.
 * @param jobId The unique ID of the Job.
 */
data class TrainState<T : Model>(
    val userOldModelPath: FilePath,
    val userNewModelPath: FilePath,
    val userDataset: Dataset,
    val userOptimizer: Optimizer,
    val userLoss: Loss,
    val userMetrics: Set<String>,
    val userEpochs: Int,
    val userNewModel: T,
    val userValidationSplit: Option<Double>,
    val generateDebugComments: Boolean,
    val target: ModelDeploymentTarget,
    val datasetPlugin: Plugin,
    val jobId: Int
) {

    // Just need to check one because of the [require] below
    val usesAWS = userOldModelPath is FilePath.S3

    init {
        val s3Check = when (userDataset) {
            is Dataset.ExampleDataset -> allS3OrLocal(userOldModelPath, userNewModelPath)
            is Dataset.Custom -> allS3OrLocal(
                userOldModelPath,
                userNewModelPath,
                userDataset.path
            )
        }

        require(s3Check) {
            "All FilePath instances must be of the same type."
        }
    }
}
