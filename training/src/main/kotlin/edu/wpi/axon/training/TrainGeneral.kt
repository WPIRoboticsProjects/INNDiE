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
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tflayerloader.DefaultLayersToGraph
import edu.wpi.axon.tflayerloader.LoadLayersFromHDF5
import java.io.File
import java.nio.file.Paths

/**
 * Trains a [Model.General].
 *
 * @param trainState The train state to pull all the configuration data from.
 */
class TrainGeneral(
    private val trainState: TrainState<Model.General>
) {

    private val userOldModelName = Paths.get(trainState.userOldModelPath).fileName.toString()

    init {
        require(userOldModelName != trainState.userNewModelName) {
            "The old model name (${userOldModelName}) cannot equal the new model " +
                "name (${trainState.userNewModelName})."
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
                val (xTrain, yTrain, xTest, yTest) = loadExampleDataset(trainState)

                // TODO: How does the user configure data preprocessing?

                val model = downloadAndLoadModel(trainState, userOldModelName)

                val newModelVar by variables.creating(Variable::class)
                val applyLayerDeltaTask by tasks.running(ApplyFunctionalLayerDeltaTask::class) {
                    modelInput = model
                    currentModel = userCurrentModel
                    newModel = trainState.userNewModel
                    newModelOutput = newModelVar
                }

                lastTask = compileTrainSaveUpload(
                    trainState,
                    userCurrentModel,
                    newModelVar,
                    applyLayerDeltaTask,
                    xTrain,
                    yTrain,
                    xTest,
                    yTest
                )
            }

            script.code(trainState.generateDebugComments)
        }.attempt().unsafeRunSync().fold(
            { Throwables.getStackTraceAsString(it).invalidNel() },
            { it }
        )
}
