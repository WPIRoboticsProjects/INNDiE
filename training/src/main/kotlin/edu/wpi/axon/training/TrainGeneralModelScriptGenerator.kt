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
                    val loadedDataset = loadDataset(trainState).let { dataset ->
                        if (trainState.userNewModel.input.size == 1) {
                            // Only try to transform the dataset if there is one input, similar to
                            // the sequential model case.

                            val modelInput = trainState.userNewModel.input.first()
                            require(modelInput.type.count { it == null } <= 1)
                            val reshapeArgsFromInputType = modelInput.type.map { it ?: -1 }
                            reshapeAndScaleLoadedDataset(dataset, reshapeArgsFromInputType, 255)
                        } else {
                            dataset
                        }
                    }

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
