package edu.wpi.axon.training

import arrow.core.Option
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.inndie.util.FilePath
import edu.wpi.inndie.util.allS3OrLocal
import edu.wpi.inndie.util.getOutputModelName
import java.nio.file.Path

/**
 * All configuration data needed to generate a training script.
 *
 * @param userOldModelPath The path to the model to load.
 * @param userDataset The dataset to train on.
 * @param userOptimizer The [Optimizer] to use.
 * @param userLoss The [Loss] function to use.
 * @param userMetrics Any metrics.
 * @param userEpochs The number of epochs.
 * @param userNewModel The new model.
 * @param generateDebugComments Whether to put debug comments in the output.
 * @param target Where the model will be deployed.
 * @param workingDir The directory the training script should work out of. New files, etc. will be
 * put in this directory.
 * @param datasetPlugin The plugin used to process the dataset after it is loaded.
 * @param jobId The unique ID of the Job.
 */
data class TrainState<T : Model>(
    val userOldModelPath: FilePath,
    val userDataset: Dataset,
    val userOptimizer: Optimizer,
    val userLoss: Loss,
    val userMetrics: Set<String>,
    val userEpochs: Int,
    val userNewModel: T,
    val userValidationSplit: Option<Double>,
    val generateDebugComments: Boolean,
    val target: ModelDeploymentTarget,
    val workingDir: Path,
    val datasetPlugin: Plugin,
    val jobId: Int
) {

    // Just need to check one because of the [require] below
    val usesAWS = userOldModelPath is FilePath.S3

    val trainedModelFilename =
        getOutputModelName(userOldModelPath.filename)

    init {
        val s3Check = when (userDataset) {
            is Dataset.ExampleDataset -> allS3OrLocal(
                userOldModelPath
            )
            is Dataset.Custom -> allS3OrLocal(
                userOldModelPath,
                userDataset.path
            )
        }

        require(s3Check) {
            "All FilePath instances must be of the same type."
        }
    }
}
