package edu.wpi.axon.ui

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.fx.IO
import arrow.fx.extensions.fx
import edu.wpi.axon.aws.EC2TrainingScriptRunner
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.training.TrainGeneralModelScriptGenerator
import edu.wpi.axon.training.TrainSequentialModelScriptGenerator
import edu.wpi.axon.training.TrainState
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ec2.model.InstanceType

/**
 * @param bucketName The S3 bucket name to use for dataset and models.
 * @param instanceType The type of the EC2 instance to run the training script on.
 * @param region The region to connect to, or `null` to autodetect the region.
 */
class JobRunner(
    private val bucketName: String,
    instanceType: InstanceType,
    private val region: Region?
) {

    private val scriptRunner = EC2TrainingScriptRunner(bucketName, instanceType, region)

    /**
     * Generates the code for a job and starts it on EC2.
     *
     * @param job The [Job] to run.
     * @return The script id of the script that was started.
     */
    fun startJob(job: Job): Long = IO.fx {
        val trainModelScriptGenerator = when (val model = job.userModel) {
            is Model.Sequential -> TrainSequentialModelScriptGenerator(toTrainState(job, model))
            is Model.General -> TrainGeneralModelScriptGenerator(toTrainState(job, model))
        }

        val script = trainModelScriptGenerator.generateScript().fold(
            {
                IO.raiseError<String>(
                    IllegalStateException(
                        """
                        |Got errors when generating script:
                        |${it.all.joinToString("\n")}
                        """.trimMargin()
                    )
                )
            },
            { IO.just(it) }
        ).bind()

        scriptRunner.startScript(
            oldModelName = trainModelScriptGenerator.trainState.userOldModelName,
            newModelName = job.userNewModelName,
            datasetPathInS3 = when (val dataset = job.userDataset) {
                is Dataset.ExampleDataset -> None
                is Dataset.Custom -> Some(dataset.pathInS3)
            },
            scriptContents = script
        ).bind()
    }.unsafeRunSync()

    private fun <T : Model> toTrainState(
        job: Job,
        model: T
    ) = TrainState(
        userOldModelPath = job.userOldModelPath,
        userNewModelName = job.userNewModelName,
        userDataset = job.userDataset,
        userOptimizer = job.userOptimizer,
        userLoss = job.userLoss,
        userMetrics = job.userMetrics,
        userEpochs = job.userEpochs,
        userValidationSplit = None, // TODO: Add this to Job and pull it from there
        userNewModel = model,
        userBucketName = bucketName,
        userRegion = Option.fromNullable(region?.id()),
        handleS3InScript = false,
        generateDebugComments = job.generateDebugComments
    )
}
