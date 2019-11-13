package edu.wpi.axon.tfdata

import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer

/**
 * All the data needed to train a model.
 *
 * @param modelPath The path to the model file.
 * @param dataset The dataset to train on.
 * @param optimizer The [Optimizer] to use.
 * @param loss The [Loss] function to use.
 * @param metrics Any metrics.
 * @param epochs The number of epochs.
 * @param newModel The new model.
 * @param generateDebugComments Whether to put debug comments in the output.
 */
data class TrainState(
    val modelPath: String,
    val dataset: Dataset,
    val optimizer: Optimizer,
    val loss: Loss,
    val metrics: Set<String>,
    val epochs: Int,
    val newModel: Model,
    val generateDebugComments: Boolean = false
)
