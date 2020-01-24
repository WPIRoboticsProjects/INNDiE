package edu.wpi.axon.training

import arrow.core.None
import arrow.core.Option
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer

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
 */
data class TrainState<T : Model>(
    val userOldModelPath: ModelPath,
    val userNewModelPath: ModelPath,
    val userDataset: Dataset,
    val userOptimizer: Optimizer,
    val userLoss: Loss,
    val userMetrics: Set<String>,
    val userEpochs: Int,
    val userNewModel: T,
    val userValidationSplit: Option<Double> = None,
    val generateDebugComments: Boolean = false
) {

    // Just need to check one because of the [require] below
    val usesAWS = userOldModelPath is ModelPath.S3

    init {
        require(
            (userOldModelPath is ModelPath.S3 && userNewModelPath is ModelPath.S3) ||
                (userOldModelPath is ModelPath.Local && userNewModelPath is ModelPath.Local)
        ) {
            "Both the old and new model paths must be of the same type (either both are S3 or " +
                "Local)."
        }
    }
}
