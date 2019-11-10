package edu.wpi.axon.training

import edu.wpi.axon.dsl.ScriptGenerator
import edu.wpi.axon.dsl.creating
import edu.wpi.axon.dsl.running
import edu.wpi.axon.dsl.task.DownloadModelFromS3Task
import edu.wpi.axon.dsl.task.LoadModelTask
import edu.wpi.axon.dsl.variable.Variable

internal fun ScriptGenerator.downloadAndLoadModel(trainState: TrainState<*>): Variable {
    val downloadModelFromS3Task by tasks.running(DownloadModelFromS3Task::class) {
        modelName = trainState.userOldModelPath
        bucketName = trainState.userBucketName
        region = trainState.userRegion
    }

    val model by variables.creating(Variable::class)
    val loadModelTask by tasks.running(LoadModelTask::class) {
        modelPath = trainState.userOldModelPath
        modelOutput = model
        dependencies += downloadModelFromS3Task
    }

    return model
}
