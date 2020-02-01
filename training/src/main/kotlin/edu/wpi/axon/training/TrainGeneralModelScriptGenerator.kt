package edu.wpi.axon.training

import arrow.core.NonEmptyList
import arrow.core.Validated
import arrow.core.invalidNel
import arrow.fx.IO
import com.google.common.base.Throwables
import edu.wpi.axon.dsl.ScriptGenerator
import edu.wpi.axon.dsl.container.DefaultPolymorphicNamedDomainObjectContainer
import edu.wpi.axon.dsl.creating
import edu.wpi.axon.dsl.runExactlyOnce
import edu.wpi.axon.dsl.running
import edu.wpi.axon.dsl.task.ApplyFunctionalLayerDeltaTask
import edu.wpi.axon.dsl.task.EnableEagerExecutionTask
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.Model
import mu.KotlinLogging

/**
 * Trains a [Model.General].
 *
 * @param trainState The train state to pull all the configuration data from.
 */
class TrainGeneralModelScriptGenerator(
    override val trainState: TrainState<Model.General>,
    private val oldModel: Model.General
) : TrainModelScriptGenerator<Model.General> {

    @Suppress("UNUSED_VARIABLE")
    override fun generateScript(): Validated<NonEmptyList<String>, String> {
        LOGGER.info {
            "Generating script with trainState:\n$trainState"
        }

        return IO {
            val script = ScriptGenerator(
                DefaultPolymorphicNamedDomainObjectContainer.of(),
                DefaultPolymorphicNamedDomainObjectContainer.of()
            ) {
                pregenerationLastTask = tasks.runExactlyOnce(EnableEagerExecutionTask::class)

                val loadedDataset = loadDataset(trainState).let { dataset ->
                    if (trainState.userNewModel.input.size == 1) {
                        // Only try to transform the dataset if there is one input, similar to
                        // the sequential model case.

                val dataset = processLoadedDatasetWithPlugin(
                    loadDataset(trainState),
                    trainState.datasetPlugin
                )

                val castDataset = when (trainState.target) {
                    ModelDeploymentTarget.Desktop -> loadedDataset
                    is ModelDeploymentTarget.Coral -> {
                        castLoadedDataset(
                            loadedDataset,
                            "tf.float32"
                        )
                    }
                }

                val model = loadModel(trainState)

                val newModelVar by variables.creating(Variable::class)
                val applyLayerDeltaTask by tasks.running(ApplyFunctionalLayerDeltaTask::class) {
                    modelInput = model
                    oldModel = this@TrainGeneralModelScriptGenerator.oldModel
                    newModel = trainState.userNewModel
                    newModelOutput = newModelVar
                }

                val compileTrainSaveTask = compileTrainSave(
                    trainState,
                    oldModel,
                    newModelVar,
                    applyLayerDeltaTask,
                    castDataset
                )

                lastTask = when (trainState.target) {
                    ModelDeploymentTarget.Desktop -> compileTrainSaveTask

                    is ModelDeploymentTarget.Coral -> {
                        val compileForEdgeTpuTask =
                            quantizeAndCompileForEdgeTpu(trainState, castDataset)
                        compileForEdgeTpuTask.dependencies.add(compileTrainSaveTask)
                        compileForEdgeTpuTask
                    }
                }
            }

            script.code(trainState.generateDebugComments)
        }.attempt().unsafeRunSync().fold(
            { Throwables.getStackTraceAsString(it).invalidNel() },
            { it }
        )
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
