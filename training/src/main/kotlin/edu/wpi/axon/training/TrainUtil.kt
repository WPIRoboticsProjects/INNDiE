@file:Suppress("UNUSED_VARIABLE")

package edu.wpi.axon.training

import arrow.core.None
import arrow.core.Some
import edu.wpi.axon.dsl.ScriptGenerator
import edu.wpi.axon.dsl.create
import edu.wpi.axon.dsl.creating
import edu.wpi.axon.dsl.run
import edu.wpi.axon.dsl.runExactlyOnce
import edu.wpi.axon.dsl.running
import edu.wpi.axon.dsl.task.CheckpointCallbackTask
import edu.wpi.axon.dsl.task.CompileModelTask
import edu.wpi.axon.dsl.task.ConvertSuperviselyDatasetToRecord
import edu.wpi.axon.dsl.task.DownloadUntrainedModelFromS3Task
import edu.wpi.axon.dsl.task.EarlyStoppingTask
import edu.wpi.axon.dsl.task.EnableEagerExecutionTask
import edu.wpi.axon.dsl.task.LoadExampleDatasetTask
import edu.wpi.axon.dsl.task.LoadModelTask
import edu.wpi.axon.dsl.task.LoadTFRecordOfImagesWithObjects
import edu.wpi.axon.dsl.task.ReshapeAndScaleTask
import edu.wpi.axon.dsl.task.S3ProgressReportingCallbackTask
import edu.wpi.axon.dsl.task.SaveModelTask
import edu.wpi.axon.dsl.task.Task
import edu.wpi.axon.dsl.task.TrainTask
import edu.wpi.axon.dsl.task.UploadTrainedModelToS3Task
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
    val downloadModelFromS3Task = if (trainState.handleS3InScript) {
        check(trainState.userBucketName != null) {
            "The script was told to download the model from S3, but no bucket name was specified."
        }

        tasks.run(DownloadUntrainedModelFromS3Task::class) {
            modelName = trainState.userOldModelPath
            bucketName = trainState.userBucketName
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
        trainState.userDataset.pathInS3.endsWith(".tar") ->
            loadSuperviselyDataset(trainState)

        else -> error(
            "Unsupported dataset format: ${trainState.userDataset.pathInS3}"
        )
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

    // TODO: Run conversion as a separate step so that eager execution is disabled when training
    // LoadTFRecordOfImagesWithObjects needs eager execution
    check(pregenerationLastTask == null) {
        "BUG: pregenerationLastTask was not null and would have been overwritten."
    }
    pregenerationLastTask = tasks.runExactlyOnce(EnableEagerExecutionTask::class)

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
            "${oldModel.name}-weights.{epoch:02d}-{val_loss:.2f}.hdf5"
        } else {
            "${oldModel.name}-weights.{epoch:02d}-{loss:.2f}.hdf5"
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

    var s3ProgressReportingCallback: Variable? = null
    if (trainState.userBucketName != null) {
        s3ProgressReportingCallback = variables.create(Variable::class)
        tasks.run(S3ProgressReportingCallbackTask::class) {
            modelName = trainState.userNewModelName
            datasetName = trainState.userDataset.nameForS3ProgressReporting
            bucketName = trainState.userBucketName
            output = s3ProgressReportingCallback
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

        callbacks = setOf(checkpointCallback, earlyStoppingCallback)

        // Add the s3 progress reporting callback if it is not null. Null when we don't have AWS
        // data.
        s3ProgressReportingCallback?.let { callbacks = callbacks + it }

        epochs = trainState.userEpochs
        dependencies += compileModelTask
    }

    val saveModelTask by tasks.running(SaveModelTask::class) {
        modelInput = newModel
        modelFileName = trainState.userNewModelName
        dependencies += trainModelTask
    }

    return if (trainState.handleS3InScript) {
        val uploadModelToS3Task by tasks.running(UploadTrainedModelToS3Task::class) {
            check(trainState.userBucketName != null) {
                "The script was told to upload the model to S3, but no bucket name was specified."
            }

            modelName = trainState.userNewModelName
            bucketName = trainState.userBucketName
            dependencies += saveModelTask
        }

        uploadModelToS3Task
    } else {
        saveModelTask
    }
}
