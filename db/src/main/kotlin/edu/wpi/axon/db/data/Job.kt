package edu.wpi.axon.db.data

import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.training.ModelDeploymentTarget

/**
 * @param userOldModelPath The path to the model to load.
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
data class Job(
    val name: String,
    val status: TrainingScriptProgress,
    val userOldModelPath: ModelSource,
    val userDataset: Dataset,
    val userOptimizer: Optimizer,
    val userLoss: Loss,
    val userMetrics: Set<String>,
    val userEpochs: Int,
    val userNewModel: Model,
    val generateDebugComments: Boolean,
    val trainingMethod: JobTrainingMethod,
    val target: ModelDeploymentTarget,
    val datasetPlugin: Plugin,
    val id: Int
)
