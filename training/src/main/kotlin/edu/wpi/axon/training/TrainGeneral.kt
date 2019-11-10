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
import edu.wpi.axon.dsl.task.EarlyStoppingTask
import edu.wpi.axon.dsl.task.LoadExampleDatasetTask
import edu.wpi.axon.dsl.task.SaveModelTask
import edu.wpi.axon.dsl.task.TrainTask
import edu.wpi.axon.dsl.task.UploadModelToS3Task
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tflayerloader.DefaultLayersToGraph
import edu.wpi.axon.tflayerloader.LoadLayersFromHDF5
import java.io.File

/**
 * Trains a [Model.General].
 *
 * @param trainState The train state to pull all the configuration data from.
 */
class TrainGeneral(
    private val trainState: TrainState<Model.General>
) {

    init {
        require(trainState.userOldModelPath != trainState.userNewModelPath) {
            "The old model path (${trainState.userOldModelPath}) cannot equal the new model " +
                "path (${trainState.userNewModelPath})."
        }
    }

    private val loadLayersFromHDF5 = LoadLayersFromHDF5(DefaultLayersToGraph())

    @Suppress("UNUSED_VARIABLE")
    fun generateScript(): Validated<NonEmptyList<String>, String> =
        loadLayersFromHDF5.load(File(trainState.userOldModelPath)).map { userCurrentModel ->
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
                    dataset = trainState.userDataset
                    xTrainOutput = xTrain
                    yTrainOutput = yTrain
                    xTestOutput = xTest
                    yTestOutput = yTest
                }

                // TODO: How does the user configure data preprocessing?

                val model = downloadAndLoadModel(trainState)

                val newModelVar by variables.creating(Variable::class)
                val applyLayerDeltaTask by tasks.running(ApplyFunctionalLayerDeltaTask::class) {
                    modelInput = model
                    currentModel = userCurrentModel
                    newModel = trainState.userNewModel
                    newModelOutput = newModelVar
                }

                val compileModelTask by tasks.running(CompileModelTask::class) {
                    modelInput = newModelVar
                    optimizer = trainState.userOptimizer
                    loss = trainState.userLoss
                    metrics = trainState.userMetrics
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
                    epochs = trainState.userEpochs
                    dependencies += compileModelTask
                }

                val saveModelTask by tasks.running(SaveModelTask::class) {
                    modelInput = newModelVar
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
