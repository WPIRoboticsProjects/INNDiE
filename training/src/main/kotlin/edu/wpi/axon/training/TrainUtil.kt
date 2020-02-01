@file:Suppress("UNUSED_VARIABLE")

package edu.wpi.axon.training

import arrow.core.None
import arrow.core.Some
import edu.wpi.axon.dsl.ScriptGenerator
import edu.wpi.axon.dsl.create
import edu.wpi.axon.dsl.creating
import edu.wpi.axon.dsl.run
import edu.wpi.axon.dsl.running
import edu.wpi.axon.dsl.task.CastTask
import edu.wpi.axon.dsl.task.CheckpointCallbackTask
import edu.wpi.axon.dsl.task.CompileModelTask
import edu.wpi.axon.dsl.task.ConvertSuperviselyDatasetToRecord
import edu.wpi.axon.dsl.task.EarlyStoppingTask
import edu.wpi.axon.dsl.task.LoadExampleDatasetTask
import edu.wpi.axon.dsl.task.LoadModelTask
import edu.wpi.axon.dsl.task.LoadTFRecordOfImagesWithObjects
import edu.wpi.axon.dsl.task.LocalProgressReportingCallbackTask
import edu.wpi.axon.dsl.task.PostTrainingQuantizationTask
import edu.wpi.axon.dsl.task.ReshapeAndScaleTask
import edu.wpi.axon.dsl.task.RunEdgeTpuCompilerTask
import edu.wpi.axon.dsl.task.RunPluginTask
import edu.wpi.axon.dsl.task.RunEdgeTpuCompilerTask
import edu.wpi.axon.dsl.task.S3ProgressReportingCallbackTask
import edu.wpi.axon.dsl.task.SaveModelTask
import edu.wpi.axon.dsl.task.SliceTask
import edu.wpi.axon.dsl.task.Task
import edu.wpi.axon.dsl.task.TrainTask
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import java.nio.file.Paths

/**
 * Loads a model in to a variable using. Assumes the model is on disk.
 *
 * @param trainState The training state.
 * @return The loaded model from [LoadModelTask].
 */
internal fun ScriptGenerator.loadModel(trainState: TrainState<*>): Variable {
    val model = variables.create(Variable::class)
    val loadModelTask = tasks.run(LoadModelTask::class) {
        modelPath = trainState.userOldModelPath.path
        modelOutput = model
    }

    return model
}

/**
 * Loads a dataset in any format.
 *
 * @param trainState The training state.
 * @return The loaded dataset.
 */
internal fun ScriptGenerator.loadDataset(
    trainState: TrainState<*>
): LoadedDataset = when (trainState.userDataset) {
    is Dataset.ExampleDataset -> loadExampleDataset(trainState)

    is Dataset.Custom -> when {
        trainState.userDataset.path.path.endsWith(".tar") -> loadSuperviselyDataset(trainState)
        else -> error("Unsupported dataset format: ${trainState.userDataset}")
    }
}

/**
 * Loads an example dataset.
 *
 * @param trainState The training state.
 * @return The loaded dataset with [LoadedDataset.validation].
 */
internal fun ScriptGenerator.loadExampleDataset(
    trainState: TrainState<*>
): LoadedDataset {
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

    return LoadedDataset(
        train = xTrain to yTrain,
        validation = Some(xTest to yTest),
        validationSplit = None
    )
}

/**
 * Loads a Supervisely dataset.
 *
 * @param trainState The training state.
 * @return The loaded dataset without [LoadedDataset.validation].
 */
internal fun ScriptGenerator.loadSuperviselyDataset(
    trainState: TrainState<*>
): LoadedDataset {
    require(trainState.userDataset is Dataset.Custom)

    val convertTask = tasks.run(ConvertSuperviselyDatasetToRecord::class) {
        dataset = trainState.userDataset
    }

    val xTrain by variables.creating(Variable::class)
    val yTrain by variables.creating(Variable::class)
    tasks.run(LoadTFRecordOfImagesWithObjects::class) {
        dataset = trainState.userDataset
        xOutput = xTrain
        yOutput = yTrain
        dependencies += convertTask
    }

    return LoadedDataset(
        train = xTrain to yTrain,
        validation = None,
        validationSplit = None
    )
}

internal fun ScriptGenerator.processDatasetWithPlugin(
    xIn: Variable,
    yIn: Variable,
    plugin: Plugin
): Pair<Variable, Variable> {
    val xOut by variables.creating(Variable::class)
    val yOut by variables.creating(Variable::class)

    tasks.run(RunPluginTask::class) {
        functionName = "process_dataset"
        functionDefinition = plugin.contents
        functionInputs = listOf(xIn, yIn)
        functionOutputs = listOf(xOut, yOut)
    }

    return xOut to yOut
}

internal fun ScriptGenerator.processLoadedDatasetWithPlugin(
    dataset: LoadedDataset,
    plugin: Plugin
): LoadedDataset {
    val processedTrain = processDatasetWithPlugin(dataset.train.first, dataset.train.second, plugin)

    val processedValidation = dataset.validation.map {
        processDatasetWithPlugin(it.first, it.second, plugin)
    }

    return dataset.copy(
        train = processedTrain,
        validation = processedValidation
    )
}

/**
 * Runs the [ReshapeAndScaleTask] on all data in the [dataset].
 *
 * @param dataset The dataset to scale.
 * @param reshapeArgs The reshape args for [ReshapeAndScaleTask.reshapeArgs].
 * @param scale The scale arg for [ReshapeAndScaleTask.scale].
 * @return A copy of [dataset] with the new data.
 */
internal fun ScriptGenerator.reshapeAndScaleLoadedDataset(
    dataset: LoadedDataset,
    reshapeArgs: List<Int>,
    scale: Int
): LoadedDataset {
    val scaledTrain = reshapeAndScale(
        dataset.train.first,
        reshapeArgs,
        scale
    ) to dataset.train.second

    val scaledValidation = dataset.validation.map {
        reshapeAndScale(it.first, reshapeArgs, scale) to it.second
    }

    return dataset.copy(
        train = scaledTrain,
        validation = scaledValidation
    )
}

internal fun ScriptGenerator.cast(
    dataset: Variable,
    dtypeIn: String
): Variable {
    val castDataset by variables.creating(Variable::class)
    tasks.run(CastTask::class) {
        input = dataset
        output = castDataset
        dtype = dtypeIn
    }

    return castDataset
}

internal fun ScriptGenerator.castLoadedDataset(
    dataset: LoadedDataset,
    dtype: String
): LoadedDataset {
    val castTrain = cast(dataset.train.first, dtype) to dataset.train.second

    val castValidation = dataset.validation.map {
        cast(it.first, dtype) to it.second
    }

    return dataset.copy(
        train = castTrain,
        validation = castValidation
    )
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
 * Compiles, trains (with callbacks), and saves a model.
 *
 * @param trainState The training state.
 * @param oldModel The model on disk the user is starting training with.
 * @param newModel The new model that will be compiled, trained, and saved.
 * @param applyLayerDeltaTask Will be depended on by the [CompileModelTask].
 * @param loadedDataset The dataset to train on.
 * @return The last task in the sequence of operations.
 */
internal fun ScriptGenerator.compileTrainSave(
    trainState: TrainState<*>,
    oldModel: Model,
    newModel: Variable,
    applyLayerDeltaTask: Task,
    loadedDataset: LoadedDataset
): Task {
    val hasValidation = loadedDataset.validation.isDefined() ||
        loadedDataset.validationSplit.fold({ false }, { it > 0.0 })

    val compileModelTask by tasks.running(CompileModelTask::class) {
        modelInput = newModel
        optimizer = trainState.userOptimizer
        loss = trainState.userLoss
        metrics = trainState.userMetrics
        dependencies += applyLayerDeltaTask
    }

    val checkpointCallback by variables.creating(Variable::class)
    tasks.run(CheckpointCallbackTask::class) {
        filePath = if (hasValidation) {
            // Can only use val_loss if there is validation data, which can take the form of
            // a validation dataset or a nonzero validation split
            "${trainState.workingDir}/${oldModel.name}-weights.{epoch:02d}-{val_loss:.2f}.hdf5"
        } else {
            "${trainState.workingDir}/${oldModel.name}-weights.{epoch:02d}-{loss:.2f}.hdf5"
        }
        monitor = if (hasValidation) "val_loss" else "loss"
        saveWeightsOnly = true
        verbose = 1
        output = checkpointCallback
    }

    val earlyStoppingCallback by variables.creating(Variable::class)
    tasks.run(EarlyStoppingTask::class) {
        monitor = if (hasValidation) "val_loss" else "loss"
        patience = 10
        verbose = 1
        output = earlyStoppingCallback
    }

    val progressReportingCallback: Variable = variables.create(Variable::class)
    if (trainState.usesAWS) {
        tasks.run(S3ProgressReportingCallbackTask::class) {
            jobId = trainState.jobId
            output = progressReportingCallback
        }
    } else {
        tasks.run(LocalProgressReportingCallbackTask::class) {
            jobId = trainState.jobId
            output = progressReportingCallback
        }
    }

    val trainModelTask by tasks.running(TrainTask::class) {
        modelInput = newModel
        trainInputData = loadedDataset.train.first
        trainOutputData = loadedDataset.train.second

        // Add validation data if it is present
        loadedDataset.validationSplit.map { validationSplit = it }
        loadedDataset.validation.map {
            validationInputData = Some(it.first)
            validationOutputData = Some(it.second)
        }

        callbacks = setOf(checkpointCallback, earlyStoppingCallback, progressReportingCallback)

        epochs = trainState.userEpochs
        dependencies += compileModelTask
    }

    return tasks.run(SaveModelTask::class) {
        modelInput = newModel
        modelPath = "${trainState.workingDir}/${trainState.trainedModelFilename}"
        dependencies += trainModelTask
    }
}

/**
 * Quantizes and compiles a model for the Coral Edge TPU. Uses post-training quantization.
 *
 * @param trainState The training state
 * @param loadedDataset The dataset the model was trained on.
 * @return The last task in the sequence of operations.
 */
internal fun ScriptGenerator.quantizeAndCompileForEdgeTpu(
    trainState: TrainState<*>,
    loadedDataset: LoadedDataset
): Task {
    require(trainState.target is ModelDeploymentTarget.Coral)

    // Only grab 10% of the dataset
    val datasetSlice = variables.create(Variable::class)
    tasks.run(SliceTask::class) {
        input = loadedDataset.train.first
        output = datasetSlice
        sliceNotation = "[:int(len(${loadedDataset.train.first.name}) * " +
            "${trainState.target.representativeDatasetPercentage})]"
    }

    val tfliteModelPath = "${trainState.trainedModelFilename.substringBeforeLast('.')}.tflite"
    val postTrainingQuantizationTask by tasks.running(PostTrainingQuantizationTask::class) {
        modelFilename = trainState.workingDir.resolve(trainState.trainedModelFilename).toString()
        outputModelFilename = trainState.workingDir.resolve(tfliteModelPath).toString()
        representativeDataset = datasetSlice
    }

    val runEdgeTpuCompilerTask by tasks.running(RunEdgeTpuCompilerTask::class) {
        inputModelFilename = tfliteModelPath
        outputDir = trainState.workingDir.toString()
        dependencies.add(postTrainingQuantizationTask)
    }

    return runEdgeTpuCompilerTask
}
