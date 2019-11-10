@file:Suppress("UNUSED_VARIABLE")

package edu.wpi.axon.training

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
import edu.wpi.axon.tfdata.Model

internal fun ScriptGenerator.loadModel(trainState: TrainState<*>, oldModelName: String): Variable {
    val downloadModelFromS3Task = if (trainState.userAuth != null) {
        tasks.run(DownloadModelFromS3Task::class) {
            modelName = trainState.userOldModelPath
            bucketName = trainState.userAuth.first
            region = trainState.userAuth.second
        }
    } else null

    val model = variables.create(Variable::class)
    val loadModelTask = tasks.run(LoadModelTask::class) {
        modelPath = oldModelName
        modelOutput = model

        if (downloadModelFromS3Task != null) {
            dependencies += downloadModelFromS3Task
        }
    }

    return model
}

internal fun ScriptGenerator.loadExampleDataset(
    trainState: TrainState<*>
): Tuple4<Variable, Variable, Variable, Variable> {
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

internal fun ScriptGenerator.compileTrainSave(
    trainState: TrainState<*>,
    currentModel: Model,
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
        filePath = "${currentModel.name}-weights.{epoch:02d}-{val_loss:.2f}.hdf5"
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
        validationInputData = xTest
        validationOutputData = yTest
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
