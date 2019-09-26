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
import edu.wpi.axon.dsl.task.LoadModelTask
import edu.wpi.axon.dsl.task.TrainTask
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.layer.SealedLayer
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.tflayerloader.DefaultLayersToGraph
import edu.wpi.axon.tflayerloader.LoadLayersFromHDF5
import java.io.File

/**
 * Trains a Sequential model.
 *
 * @param userModelPath The path to the model file.
 * @param userDataset The dataset to train on.
 * @param userOptimizer The [Optimizer] to use.
 * @param userLoss The [Loss] function to use.
 * @param userMetrics Any metrics.
 * @param userEpochs The number of epochs.
 * @param userNewLayers The new layers (in the case of transfer learning).
 * @param generateDebugComments Whether to put debug comments in the output.
 */
class TrainGeneral(
    private val userModelPath: String,
    private val userDataset: Dataset,
    private val userOptimizer: Optimizer,
    private val userLoss: Loss,
    private val userMetrics: Set<String>,
    private val userEpochs: Int,
    private val userNewLayers: Set<SealedLayer.MetaLayer>,
    private val generateDebugComments: Boolean = false
) {

    private val loadLayersFromHDF5 = LoadLayersFromHDF5(DefaultLayersToGraph())

    @Suppress("UNUSED_VARIABLE")
    fun generateScript(): Validated<NonEmptyList<String>, String> =
        loadLayersFromHDF5.load(File(userModelPath)).map { currentModel ->
            require(currentModel is Model.General)

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

                val model by variables.creating(Variable::class)
                val loadModelTask by tasks.running(LoadModelTask::class) {
                    modelPath = userModelPath.substringAfterLast('/')
                    modelOutput = model
                }

                val newModel by variables.creating(Variable::class)
                val applyLayerDeltaTask by tasks.running(ApplyFunctionalLayerDeltaTask::class) {
                    modelInput = model
                    currentLayers = currentModel.layers.nodes() // TODO: Accept the graph directly
                    newLayers = userNewLayers
                    newModelOutput = newModel
                }

                val compileModelTask by tasks.running(CompileModelTask::class) {
                    modelInput = newModel
                    optimizer = userOptimizer
                    loss = userLoss
                    metrics = userMetrics
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
                    trainInputData = xTrain
                    trainOutputData = yTrain
                    validationInputData = xTest
                    validationOutputData = yTest
                    callbacks = setOf(checkpointCallback, earlyStoppingCallback)
                    epochs = userEpochs
                    dependencies += compileModelTask
                }

                lastTask = trainModelTask
            }

            script.code(generateDebugComments)
        }.attempt().unsafeRunSync().fold(
            { Throwables.getStackTraceAsString(it).invalidNel() },
            { it }
        )
}
