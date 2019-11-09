package edu.wpi.axon.training

import arrow.core.NonEmptyList
import arrow.core.Validated
import arrow.core.invalidNel
import com.google.common.base.Throwables
import edu.wpi.axon.dsl.ScriptGenerator
import edu.wpi.axon.dsl.container.DefaultPolymorphicNamedDomainObjectContainer
import edu.wpi.axon.dsl.creating
import edu.wpi.axon.dsl.running
import edu.wpi.axon.dsl.task.ApplyFunctionalLayerDeltaTask
import edu.wpi.axon.dsl.task.CheckpointCallbackTask
import edu.wpi.axon.dsl.task.CompileModelTask
import edu.wpi.axon.dsl.task.DownloadModelFromS3Task
import edu.wpi.axon.dsl.task.EarlyStoppingTask
import edu.wpi.axon.dsl.task.LoadExampleDatasetTask
import edu.wpi.axon.dsl.task.LoadModelTask
import edu.wpi.axon.dsl.task.SaveModelTask
import edu.wpi.axon.dsl.task.TrainTask
import edu.wpi.axon.dsl.task.UploadModelToS3Task
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.tflayerloader.DefaultLayersToGraph
import edu.wpi.axon.tflayerloader.LoadLayersFromHDF5
import java.io.File

/**
 * Trains a [Model.General].
 *
 * @param userOldModelPath The name of the model to load.
 * @param userNewModelPath The name of the model to save to.
 * @param userDataset The dataset to train on.
 * @param userOptimizer The [Optimizer] to use.
 * @param userLoss The [Loss] function to use.
 * @param userMetrics Any metrics.
 * @param userEpochs The number of epochs.
 * @param userNewModel The new model.
 * @param generateDebugComments Whether to put debug comments in the output.
 */
class TrainGeneral(
    private val userOldModelPath: String,
    private val userNewModelPath: String,
    private val userBucketName: String,
    private val userRegion: String,
    private val userDataset: Dataset,
    private val userOptimizer: Optimizer,
    private val userLoss: Loss,
    private val userMetrics: Set<String>,
    private val userEpochs: Int,
    private val userNewModel: Model.General,
    private val generateDebugComments: Boolean = false
) {

    init {
        require(userOldModelPath != userNewModelPath) {
            "The old model path ($userOldModelPath) cannot equal the new model " +
                "path ($userNewModelPath)."
        }
    }

    private val loadLayersFromHDF5 = LoadLayersFromHDF5(DefaultLayersToGraph())
    private val userOldModelName = userOldModelPath.substringAfterLast('/')

    @Suppress("UNUSED_VARIABLE")
    fun generateScript(): Validated<NonEmptyList<String>, String> =
        loadLayersFromHDF5.load(File(userOldModelPath)).map { userCurrentModel ->
            require(userCurrentModel is Model.General)

            val script = ScriptGenerator(
                DefaultPolymorphicNamedDomainObjectContainer.of(),
                DefaultPolymorphicNamedDomainObjectContainer.of()
            ) {
                val xTrain by variables.creating(Variable::class)
                val yTrain by variables.creating(Variable::class)
                val xTest by variables.creating(Variable::class)
                val yTest by variables.creating(Variable::class)
                val loadMnistDataTask by tasks.running(LoadExampleDatasetTask::class) {
                    dataset = userDataset
                    xTrainOutput = xTrain
                    yTrainOutput = yTrain
                    xTestOutput = xTest
                    yTestOutput = yTest
                }

                // TODO: How does the user configure data preprocessing?

                val downloadModelFromS3Task by tasks.running(DownloadModelFromS3Task::class) {
                    modelName = userOldModelName
                    bucketName = userBucketName
                    region = userRegion
                }

                val model by variables.creating(Variable::class)
                val loadModelTask by tasks.running(LoadModelTask::class) {
                    modelPath = userOldModelName
                    modelOutput = model
                    dependencies += downloadModelFromS3Task
                }

                val newModelVar by variables.creating(Variable::class)
                val applyLayerDeltaTask by tasks.running(ApplyFunctionalLayerDeltaTask::class) {
                    modelInput = model
                    currentModel = userCurrentModel
                    newModel = userNewModel
                    newModelOutput = newModelVar
                }

                val compileModelTask by tasks.running(CompileModelTask::class) {
                    modelInput = newModelVar
                    optimizer = userOptimizer
                    loss = userLoss
                    metrics = userMetrics
                    dependencies += applyLayerDeltaTask
                }

                val checkpointCallback by variables.creating(Variable::class)
                val checkpointCallbackTask by tasks.running(CheckpointCallbackTask::class) {
                    filePath = "${userCurrentModel.name}-weights.{epoch:02d}-{val_loss:.2f}.hdf5"
                    saveWeightsOnly = true
                    verbose = 1
                    output = checkpointCallback
                }

                val earlyStoppingCallback by variables.creating(Variable::class)
                val earlyStoppingCallbackTask by tasks.running(EarlyStoppingTask::class) {
                    patience = 10
                    verbose = 1
                    output = earlyStoppingCallback
                }

                val trainModelTask by tasks.running(TrainTask::class) {
                    modelInput = newModelVar
                    trainInputData = xTrain
                    trainOutputData = yTrain
                    validationInputData = xTest
                    validationOutputData = yTest
                    callbacks = setOf(checkpointCallback, earlyStoppingCallback)
                    epochs = userEpochs
                    dependencies += compileModelTask
                }

                val saveModelTask by tasks.running(SaveModelTask::class) {
                    modelInput = newModelVar
                    modelFileName = userNewModelPath
                    dependencies += trainModelTask
                }

                val uploadModelToS3Task by tasks.running(UploadModelToS3Task::class) {
                    modelName = userNewModelPath
                    bucketName = userBucketName
                    region = userRegion
                    dependencies += saveModelTask
                }

                lastTask = uploadModelToS3Task
            }

            script.code(generateDebugComments)
        }.attempt().unsafeRunSync().fold(
            { Throwables.getStackTraceAsString(it).invalidNel() },
            { it }
        )
}
