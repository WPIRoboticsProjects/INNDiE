package edu.wpi.inndie.db.data

import edu.wpi.inndie.plugin.Plugin
import edu.wpi.inndie.tfdata.Dataset
import edu.wpi.inndie.tfdata.Model
import edu.wpi.inndie.tfdata.loss.Loss
import edu.wpi.inndie.tfdata.optimizer.Optimizer
import edu.wpi.inndie.training.ModelDeploymentTarget

/**
 * @param userOldModelPath The path to the model to load.
 * @param userDataset The dataset to train on.
 * @param userOptimizer The [Optimizer] to use.
 * @param userLoss The [Loss] function to use.
 * @param userMetrics Any metrics.
 * @param userEpochs The number of epochs.
 * @param userNewModel The new model configuration (the old model after it was configured by the
 * user).
 * @param userNewModelFilename The filename of the new model (after training).
 * @param generateDebugComments Whether to put debug comments in the output.
 * @param internalTrainingMethod Do not set this directly, this should always start as
 * [InternalJobTrainingMethod.Untrained]. If you want to control where the Job is trained, set the
 * desired training method when the Job is started. This is the method used to train the Job, used
 * to resume progress updates if INNDiE is closed while Jobs are still running. This value is managed
 * by the backend.
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
    val userNewModelFilename: String,
    val generateDebugComments: Boolean,
    val internalTrainingMethod: InternalJobTrainingMethod,
    val target: ModelDeploymentTarget,
    val datasetPlugin: Plugin,
    val id: Int
)
