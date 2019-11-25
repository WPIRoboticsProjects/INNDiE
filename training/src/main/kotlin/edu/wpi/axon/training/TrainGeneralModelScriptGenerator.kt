package edu.wpi.axon.training

import arrow.core.NonEmptyList
import arrow.core.Validated
import arrow.core.invalidNel
import arrow.fx.IO
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

/**
 * Trains a [Model.General].
 *
 * @param trainState The train state to pull all the configuration data from.
 */
class TrainGeneralModelScriptGenerator(
    override val trainState: TrainState<Model.General>
) : TrainModelScriptGenerator<Model.General> {

    init {
        require(trainState.userOldModelName != trainState.userNewModelName) {
            "The old model name (${trainState.userOldModelName}) cannot equal the new model " +
                "name (${trainState.userNewModelName})."
        }
    }

    private val loadLayersFromHDF5 = LoadLayersFromHDF5(DefaultLayersToGraph())

    @Suppress("UNUSED_VARIABLE")
    override fun generateScript(): Validated<NonEmptyList<String>, String> =
        loadLayersFromHDF5.load(File(trainState.userOldModelPath)).flatMap { userOldModel ->
            IO {
                require(userOldModel is Model.General)

                val script = ScriptGenerator(
                    DefaultPolymorphicNamedDomainObjectContainer.of(),
                    DefaultPolymorphicNamedDomainObjectContainer.of()
                ) {
                    // TODO: Enable eager execution mode
                    val loadedDataset = loadDataset(trainState)

                    val model = loadModel(trainState)

                    val newModelVar by variables.creating(Variable::class)
                    val applyLayerDeltaTask by tasks.running(ApplyFunctionalLayerDeltaTask::class) {
                        modelInput = model
                        oldModel = userOldModel
                        newModel = trainState.userNewModel
                        newModelOutput = newModelVar
                    }

                    lastTask = compileTrainSave(
                        trainState,
                        userOldModel,
                        newModelVar,
                        applyLayerDeltaTask,
                        loadedDataset
                    )
                }

                script.code(trainState.generateDebugComments)
            }
        }.attempt().unsafeRunSync().fold(
            { Throwables.getStackTraceAsString(it).invalidNel() },
            { it }
        )
}
