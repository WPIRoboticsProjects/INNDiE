@file:Suppress("UNUSED_VARIABLE")

package edu.wpi.axon.training

import arrow.core.Some
import arrow.core.Tuple4
import edu.wpi.axon.dsl.ScriptGenerator
import edu.wpi.axon.dsl.create
import edu.wpi.axon.dsl.creating
import edu.wpi.axon.dsl.run
import edu.wpi.axon.dsl.running
import edu.wpi.axon.dsl.task.CheckpointCallbackTask
import edu.wpi.axon.dsl.task.CompileModelTask
import edu.wpi.axon.dsl.task.DownloadModelFromS3Task
import edu.wpi.axon.dsl.task.EarlyStoppingTask
import edu.wpi.axon.dsl.task.LoadExampleDatasetTask
import edu.wpi.axon.dsl.task.LoadModelTask
import edu.wpi.axon.dsl.task.ReshapeAndScaleTask
import edu.wpi.axon.dsl.task.SaveModelTask
import edu.wpi.axon.dsl.task.Task
import edu.wpi.axon.dsl.task.TrainTask
import edu.wpi.axon.dsl.task.UploadModelToS3Task
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model

/**
 * Loads a model in to a variable using. Downloads the model from S3 first if credentials were
 * provided.
 *
 * @param trainState The training state.
 * @return The loaded model from [LoadModelTask].
 */
internal fun ScriptGenerator.loadModel(trainState: TrainState<*>): Variable {
    val downloadModelFromS3Task = if (trainState.userAuth != null) {
        tasks.run(DownloadModelFromS3Task::class) {
            modelName = trainState.userOldModelPath
            bucketName = trainState.userAuth.first
            region = trainState.userAuth.second
        }
    } else null

    val model = variables.create(Variable::class)
    val loadModelTask = tasks.run(LoadModelTask::class) {
        modelPath = trainState.userOldModelName
        modelOutput = model

        if (downloadModelFromS3Task != null) {
            dependencies += downloadModelFromS3Task
        }
    }

    return model
}

/**
 * Loads an example dataset into variables.
 *
 * @param trainState The training state.
 * @return A tuple in the format `{xTrain, yTrain, xTest, yTest}`.
 */
internal fun ScriptGenerator.loadExampleDataset(
    trainState: TrainState<*>
): Tuple4<Variable, Variable, Variable, Variable> {
    require(trainState.userDataset is Dataset.ExampleDataset)

    val xTrain by variables.creating(Variable::class)
    val yTrain by variables.creating(Variable::class)
    val xTest by variables.creating(Variable::class)
    val yTest by variables.creating(Variable::class)

    tasks.run(LoadExampleDatasetTask::class) {
        dataset = trainState.userDataset
        xTrainOutput = xTrain
        yTrainOutput = yTrain
        xTestOutput = xTest
        yTestOutput = yTest
    }

    return Tuple4(xTrain, yTrain, xTest, yTest)
}

/**
 * Runs the [ReshapeAndScaleTask] on the input.
 *
 * @param dataset The dataset for [ReshapeAndScaleTask.input].
 * @param reshapeArgsIn The reshape args for [ReshapeAndScaleTask.reshapeArgs].
 * @param scaleIn The scale arg for [ReshapeAndScaleTask.scale].
 * @return The [ReshapeAndScaleTask.output].
 */
internal fun ScriptGenerator.reshapeAndScale(
    dataset: Variable,
    reshapeArgsIn: List<Int>,
    scaleIn: Number?
): Variable {
    val scaledDataset by variables.creating(Variable::class)
    tasks.run(ReshapeAndScaleTask::class) {
        input = dataset
        output = scaledDataset
        reshapeArgs = reshapeArgsIn
        scale = scaleIn
    }

    return scaledDataset
}

/**
 * Compiles, trains (with callbacks), and saves a model. Also uploads the model to S3 if credentials
 * were provided.
 *
 * @param trainState The training state.
 * @param oldModel The model on disk the user is starting training with.
 * @param newModel The new model that will be compiled, trained, and saved.
 * @param applyLayerDeltaTask Will be depended on by the [CompileModelTask].
 * @param xTrain The x-axis train data.
 * @param yTrain The y-axis train data.
 * @param xTest The x-axis test data.
 * @param yTest The y-axis test data.
 * @return The last task in the sequence of operations.
 */
internal fun ScriptGenerator.compileTrainSave(
    trainState: TrainState<*>,
    oldModel: Model,
    newModel: Variable,
    applyLayerDeltaTask: Task,
    xTrain: Variable,
    yTrain: Variable,
    xTest: Variable,
    yTest: Variable
): Task {
    val compileModelTask by tasks.running(CompileModelTask::class) {
        modelInput = newModel
        optimizer = trainState.userOptimizer
        loss = trainState.userLoss
        metrics = trainState.userMetrics
        dependencies += applyLayerDeltaTask
    }

    val checkpointCallback by variables.creating(Variable::class)
    tasks.run(CheckpointCallbackTask::class) {
        filePath = "${oldModel.name}-weights.{epoch:02d}-{val_loss:.2f}.hdf5"
        saveWeightsOnly = true
        verbose = 1
        output = checkpointCallback
    }

    val earlyStoppingCallback by variables.creating(Variable::class)
    tasks.run(EarlyStoppingTask::class) {
        patience = 10
        verbose = 1
        output = earlyStoppingCallback
    }

    val trainModelTask by tasks.running(TrainTask::class) {
        modelInput = newModel
        trainInputData = xTrain
        trainOutputData = yTrain
        validationInputData = Some(xTest)
        validationOutputData = Some(yTest)
        callbacks = setOf(checkpointCallback, earlyStoppingCallback)
        epochs = trainState.userEpochs
        dependencies += compileModelTask
    }

    val saveModelTask by tasks.running(SaveModelTask::class) {
        modelInput = newModel
        modelFileName = trainState.userNewModelName
        dependencies += trainModelTask
    }

    return if (trainState.userAuth != null) {
        val uploadModelToS3Task by tasks.running(UploadModelToS3Task::class) {
            modelName = trainState.userNewModelName
            bucketName = trainState.userAuth.first
            region = trainState.userAuth.second
            dependencies += saveModelTask
        }

        uploadModelToS3Task
    } else {
        saveModelTask
    }
}
