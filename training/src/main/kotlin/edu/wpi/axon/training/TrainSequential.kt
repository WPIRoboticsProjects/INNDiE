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
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tflayerloader.DefaultLayersToGraph
import edu.wpi.axon.tflayerloader.LoadLayersFromHDF5
import java.io.File
import java.nio.file.Paths

/**
 * Trains a [Model.Sequential].
 *
 * @param trainState The train state to pull all the configuration data from.
 */
@Suppress("UNUSED_VARIABLE")
class TrainSequential(
    private val trainState: TrainState<Model.Sequential>
) {

    private val userOldModelName = Paths.get(trainState.userOldModelPath).fileName.toString()

    init {
        require(userOldModelName != trainState.userNewModelName) {
            "The old model name (${userOldModelName}) cannot equal the new model " +
                "name (${trainState.userNewModelName})."
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
                val scaledXTrain = reshapeAndScale(xTrain, reshapeArgsFromBatchShape, 255)
                val scaledXTest = reshapeAndScale(xTest, reshapeArgsFromBatchShape, 255)

                val model = loadModel(trainState, userOldModelName)

                val newModel by variables.creating(Variable::class)
                val applyLayerDeltaTask by tasks.running(ApplySequentialLayerDeltaTask::class) {
                    modelInput = model
                    currentLayers = currentModel.layers
                    newLayers = trainState.userNewModel.layers
                    newModelOutput = newModel
                }

                lastTask = compileTrainSave(
                    trainState,
                    currentModel,
                    newModel,
                    applyLayerDeltaTask,
                    scaledXTrain,
                    yTrain,
                    scaledXTest,
                    yTest
                )
            }

            script.code(trainState.generateDebugComments)
        }.attempt().unsafeRunSync().fold(
            { Throwables.getStackTraceAsString(it).invalidNel() },
            { it }
        )
}
