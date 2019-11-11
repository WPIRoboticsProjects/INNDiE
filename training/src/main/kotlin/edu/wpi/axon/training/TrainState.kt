package edu.wpi.axon.training

import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import java.nio.file.Paths

/**
 * All configuration data needed to generate a training script.
 *
 * @param userOldModelPath The path to the model to load.
 * @param userNewModelName The name of the model to save to.
 * @param userDataset The dataset to train on.
 * @param userOptimizer The [Optimizer] to use.
 * @param userLoss The [Loss] function to use.
 * @param userMetrics Any metrics.
 * @param userEpochs The number of epochs.
 * @param userNewModel The new model.
 * @param userAuth The name of the S3 bucket to download/upload models from/to and the AWS region
 * to authenticate to, or `null` if the model should be trained entirely locally.
 * @param generateDebugComments Whether to put debug comments in the output.
 */
data class TrainState<T : Model>(
    val userOldModelPath: String,
    val userNewModelName: String,
    val userDataset: Dataset,
    val userOptimizer: Optimizer,
    val userLoss: Loss,
    val userMetrics: Set<String>,
    val userEpochs: Int,
    val userNewModel: T,
    val userAuth: Pair<String, String>? = null,
    val generateDebugComments: Boolean = false
) {

    /**
     * The filename (with extension) of the old model.
     */
    val userOldModelName: String
        get() = Paths.get(userOldModelPath).fileName.toString()
}
