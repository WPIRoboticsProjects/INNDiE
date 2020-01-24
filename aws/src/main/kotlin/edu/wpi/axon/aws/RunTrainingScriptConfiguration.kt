package edu.wpi.axon.aws

import arrow.core.None
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.training.ModelPath

/**
 * The configuration data needed to run a training script.
 *
 * @param oldModelName The name of the current model (that will be loaded).
 * @param newModelName The name of the new model (that will be trained and saved).
 * @param dataset The path to the dataset in S3, or [None] if the dataset does not need
 * to be downloaded first.
 * @param scriptContents The contents of the training script.
 * @param epochs The number of epochs the model will be trained for.
 */
data class RunTrainingScriptConfiguration(
    val oldModelName: ModelPath,
    val newModelName: ModelPath,
    val dataset: Dataset,
    val scriptContents: String,
    val epochs: Int
)
