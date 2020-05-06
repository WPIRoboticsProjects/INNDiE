package edu.wpi.axon.aws

import edu.wpi.inndie.tfdata.Dataset
import edu.wpi.inndie.util.FilePath
import java.nio.file.Path

/**
 * The configuration data needed to run a training script.
 *
 * @param oldModelName The path to the current model (that will be loaded).
 * @param dataset The path to the dataset.
 * @param scriptContents The contents of the training script.
 * @param epochs The number of epochs the model will be trained for. Must be greater than zero.
 * @param id The id of the Job this script is associated with.
 */
data class RunTrainingScriptConfiguration(
    val oldModelName: FilePath,
    val dataset: Dataset,
    val scriptContents: String,
    val epochs: Int,
    val workingDir: Path,
    val id: Int
)
