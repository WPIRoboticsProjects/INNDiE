package edu.wpi.axon.training

import arrow.core.NonEmptyList
import arrow.core.Validated
import arrow.core.invalidNel
import com.google.common.base.Throwables
import edu.wpi.axon.dsl.ScriptGenerator
import edu.wpi.axon.dsl.container.DefaultPolymorphicNamedDomainObjectContainer
import edu.wpi.axon.dsl.creating
import edu.wpi.axon.dsl.running
import edu.wpi.axon.dsl.task.ApplySequentialLayerDeltaTask
import edu.wpi.axon.dsl.task.CheckpointCallbackTask
import edu.wpi.axon.dsl.task.CompileModelTask
import edu.wpi.axon.dsl.task.EarlyStoppingTask
import edu.wpi.axon.dsl.task.ReshapeAndScaleTask
import edu.wpi.axon.dsl.task.SaveModelTask
import edu.wpi.axon.dsl.task.TrainTask
import edu.wpi.axon.dsl.task.UploadModelToS3Task
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tflayerloader.DefaultLayersToGraph
import edu.wpi.axon.tflayerloader.LoadLayersFromHDF5
import java.io.File

/**
 * Trains a [Model.Sequential].
 *
 * @param trainState The train state to pull all the configuration data from.
 */
@Suppress("UNUSED_VARIABLE")
class TrainSequential(
    private val trainState: TrainState<Model.Sequential>
) {

    init {
        require(trainState.userOldModelPath != trainState.userNewModelPath) {
            "The old model path (${trainState.userOldModelPath}) cannot equal the new model " +
                "path (${trainState.userNewModelPath})."
        }
    }

    private val loadLayersFromHDF5 = LoadLayersFromHDF5(DefaultLayersToGraph())

    fun generateScript(): Validated<NonEmptyList<String>, String> =
        loadLayersFromHDF5.load(File(trainState.userOldModelPath)).map { currentModel ->
            require(currentModel is Model.Sequential)

            require(trainState.userNewModel.batchInputShape.count { it == null } <= 1)
            val reshapeArgsFromBatchShape = trainState.userNewModel.batchInputShape.map { it ?: -1 }

            val script = ScriptGenerator(
                DefaultPolymorphicNamedDomainObjectContainer.of(),
                DefaultPolymorphicNamedDomainObjectContainer.of()
            ) {
                val (xTrain, yTrain, xTest, yTest) = loadExampleDataset(trainState)

                // TODO: How does the user configure this preprocessing?
                val scaledXTrain by variables.creating(Variable::class)
                val reshapeAndScaleXTrainTask by tasks.running(ReshapeAndScaleTask::class) {
                    input = xTrain
                    output = scaledXTrain
                    reshapeArgs = reshapeArgsFromBatchShape
                    scale = 255
                }

                val scaledXTest by variables.creating(Variable::class)
                val reshapeAndScaleXTestTask by tasks.running(ReshapeAndScaleTask::class) {
                    input = xTest
                    output = scaledXTest
                    reshapeArgs = reshapeArgsFromBatchShape
                    scale = 255
                }

                val model = downloadAndLoadModel(trainState)

                val newModel by variables.creating(Variable::class)
                val applyLayerDeltaTask by tasks.running(ApplySequentialLayerDeltaTask::class) {
                    modelInput = model
                    currentLayers = currentModel.layers
                    newLayers = trainState.userNewModel.layers
                    newModelOutput = newModel
                }

                val compileModelTask by tasks.running(CompileModelTask::class) {
                    modelInput = newModel
                    optimizer = trainState.userOptimizer
                    loss = trainState.userLoss
                    metrics = trainState.userMetrics
                    dependencies += applyLayerDeltaTask
                }

                val checkpointCallback by variables.creating(Variable::class)
                val checkpointCallbackTask by tasks.running(CheckpointCallbackTask::class) {
                    filePath = "${currentModel.name}-weights.{epoch:02d}-{val_loss:.2f}.hdf5"
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
                    modelInput = newModel
                    trainInputData = scaledXTrain
                    trainOutputData = yTrain
                    validationInputData = scaledXTest
                    validationOutputData = yTest
                    callbacks = setOf(checkpointCallback, earlyStoppingCallback)
                    epochs = trainState.userEpochs
                    dependencies += compileModelTask
                }

                val saveModelTask by tasks.running(SaveModelTask::class) {
                    modelInput = newModel
                    modelFileName = trainState.userNewModelPath
                    dependencies += trainModelTask
                }

                val uploadModelToS3Task by tasks.running(UploadModelToS3Task::class) {
                    modelName = trainState.userNewModelPath
                    bucketName = trainState.userBucketName
                    region = trainState.userRegion
                    dependencies += saveModelTask
                }

                lastTask = uploadModelToS3Task
            }

            script.code(trainState.generateDebugComments)
        }.attempt().unsafeRunSync().fold(
            { Throwables.getStackTraceAsString(it).invalidNel() },
            { it }
        )
}
