package edu.wpi.axon.db.data

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.util.FilePath
import edu.wpi.axon.util.allS3OrLocal

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
 * @param trainingMethod The method used to train the Job, used to resume progress updates if Axon
 * is closed while Jobs are still running.
 * @param datasetPlugin The plugin used to process the dataset after it is loaded.
 * @param id The database-generated unique id. Do not modify.
 */
data class Job internal constructor(
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
    var trainingMethod: JobTrainingMethod,
    var datasetPlugin: Plugin,
    var id: Int
) {

    /**
     * Whether the Job uses AWS for anything. Is [None] if the Job configuration is incorrect
     * (mixing AWS and local). Is [Some] if the configuration is correct.
     */
    val usesAWS: Option<Boolean>
        get() {
            val s3Check = when (val dataset = userDataset) {
                is Dataset.ExampleDataset -> allS3OrLocal(userOldModelPath, userNewModelName)
                is Dataset.Custom -> allS3OrLocal(
                    userOldModelPath,
                    userNewModelName,
                    dataset.path
                )
            }

            return if (s3Check) {
                // Just need to check one because of the check above
                Some(userOldModelPath is FilePath.S3)
            } else {
                // If the check failed then the configuration is wrong
                None
            }
        }
}
