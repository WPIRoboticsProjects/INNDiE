package edu.wpi.axon.training

import arrow.data.ValidatedNel
import edu.wpi.axon.dsl.ScriptGenerator
import edu.wpi.axon.dsl.container.DefaultPolymorphicNamedDomainObjectContainer
import edu.wpi.axon.dsl.creating
import edu.wpi.axon.dsl.running
import edu.wpi.axon.dsl.task.ApplyLayerDeltaTask
import edu.wpi.axon.dsl.task.CompileModelTask
import edu.wpi.axon.dsl.task.LoadExampleDatasetTask
import edu.wpi.axon.dsl.task.LoadModelTask
import edu.wpi.axon.dsl.task.ReshapeAndScaleTask
import edu.wpi.axon.dsl.task.TrainTask
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.layer.SealedLayer
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import java.io.File

/**
 * Trains a model.
 *
 * @param userModelPath The path to the model file.
 * @param userDataset The dataset to train on.
 * @param userOptimizer The [Optimizer] to use.
 * @param userLoss The [Loss] function to use.
 * @param userMetrics Any metrics.
 * @param userEpochs The number of epochs.
 * @param userCurrentLayers The current layers in the model.
 * @param userNewLayers The new layers (in the case of transfer learning).
 * @param generateDebugComments Whether to put debug comments in the output.
 */
class Training(
    private val userModelPath: String,
    private val userDataset: Dataset,
    private val userOptimizer: Optimizer,
    private val userLoss: Loss,
    private val userMetrics: Set<String>,
    private val userEpochs: Int,
    private val userCurrentLayers: Set<SealedLayer.MetaLayer>,
    private val userNewLayers: Set<SealedLayer.MetaLayer>,
    private val generateDebugComments: Boolean = false
) {

    fun generateScript(): ValidatedNel<String, String> {
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

            // TODO: How does the user configure this preprocessing?
            val scaledXTrain by variables.creating(Variable::class)
            val reshapeAndScaleXTrainTask by tasks.running(ReshapeAndScaleTask::class) {
                input = xTrain
                output = scaledXTrain
                reshapeArgs = listOf(-1, 28, 28, 1)
                scale = 255
            }

            val scaledXTest by variables.creating(Variable::class)
            val reshapeAndScaleXTestTask by tasks.running(ReshapeAndScaleTask::class) {
                input = xTest
                output = scaledXTest
                reshapeArgs = listOf(-1, 28, 28, 1)
                scale = 255
            }

            val model by variables.creating(Variable::class)
            val loadModelTask by tasks.running(LoadModelTask::class) {
                modelPath = userModelPath.substringAfterLast(File.separator)
                modelOutput = model
            }

            val newModel by variables.creating(Variable::class)
            val applyLayerDeltaTask by tasks.running(ApplyLayerDeltaTask::class) {
                modelInput = model
                currentLayers = userCurrentLayers
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

            val trainModelTask by tasks.running(TrainTask::class) {
                modelInput = newModel
                trainInputData = scaledXTrain
                trainOutputData = yTrain
                validationInputData = scaledXTest
                validationOutputData = yTest
                epochs = userEpochs
                dependencies += compileModelTask
            }

            lastTask = trainModelTask
        }

        return script.code(generateDebugComments)
    }
}
