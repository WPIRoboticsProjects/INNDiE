package edu.wpi.axon.training

import arrow.core.ValidatedNel
import arrow.core.invalidNel
import com.google.common.base.Throwables
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.tflayerloader.DefaultLayersToGraph
import edu.wpi.axon.tflayerloader.LoadLayersFromHDF5
import java.io.File

/**
 * Trains a [Model].
 *
 * @param userModelPath The path to the model file.
 * @param userDataset The dataset to train on.
 * @param userOptimizer The [Optimizer] to use.
 * @param userLoss The [Loss] function to use.
 * @param userMetrics Any metrics.
 * @param userEpochs The number of epochs.
 * @param userNewModel The new model.
 * @param generateDebugComments Whether to put debug comments in the output.
 */
class Train(
    private val userModelPath: String,
    private val userBucketName: String,
    private val userRegion: String,
    private val userDataset: Dataset,
    private val userOptimizer: Optimizer,
    private val userLoss: Loss,
    private val userMetrics: Set<String>,
    private val userEpochs: Int,
    private val userNewModel: Model,
    private val generateDebugComments: Boolean = false
) {

    private val loadLayersFromHDF5 = LoadLayersFromHDF5(DefaultLayersToGraph())

    fun generateScript(): ValidatedNel<String, String> =
        loadLayersFromHDF5.load(File(userModelPath)).map { userCurrentModel ->
            when (userCurrentModel) {
                is Model.Sequential -> {
                    if (userNewModel is Model.Sequential) {
                        TrainSequential(
                            userModelPath = userModelPath,
                            userBucketName = userBucketName,
                            userRegion = userRegion,
                            userDataset = userDataset,
                            userOptimizer = userOptimizer,
                            userLoss = userLoss,
                            userMetrics = userMetrics,
                            userEpochs = userEpochs,
                            userNewLayers = userCurrentModel.layers,
                            generateDebugComments = generateDebugComments
                        ).generateScript()
                    } else {
                        ("userNewModel must be Sequential because userCurrentModel " +
                            "is Sequential.").invalidNel()
                    }
                }

                is Model.General -> {
                    if (userNewModel is Model.General) {
                        TrainGeneral(
                            userModelPath = userModelPath,
                            userDataset = userDataset,
                            userOptimizer = userOptimizer,
                            userLoss = userLoss,
                            userMetrics = userMetrics,
                            userEpochs = userEpochs,
                            userNewModel = userNewModel
                        ).generateScript()
                    } else {
                        ("userNewModel must be General because userCurrentModel is " +
                            "General").invalidNel()
                    }
                }
            }
        }.attempt().unsafeRunSync().fold(
            { Throwables.getStackTraceAsString(it).invalidNel() },
            { it }
        )
}
