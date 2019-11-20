package edu.wpi.axon.ui

import arrow.fx.IO
import arrow.fx.extensions.fx
import edu.wpi.axon.aws.EC2TrainingScriptRunner
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tflayerloader.DefaultLayersToGraph
import edu.wpi.axon.tflayerloader.LoadLayersFromHDF5
import edu.wpi.axon.training.TrainGeneralModelScriptGenerator
import edu.wpi.axon.training.TrainSequentialModelScriptGenerator
import edu.wpi.axon.training.TrainState
import java.nio.file.Paths
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ec2.model.InstanceType

/**
 * @param bucketName The S3 bucket name to use for dataset and models.
 * @param instanceType The type of the EC2 instance to run the training script on.
 * @param region The region to connect to, or `null` to autodetect the region.
 */
class JobRunner(
    bucketName: String,
    instanceType: InstanceType,
    region: Region?
) {

    private val loadLayersFromHDF5 = LoadLayersFromHDF5(DefaultLayersToGraph())
    private val scriptRunner = EC2TrainingScriptRunner(bucketName, instanceType, region)

    /**
     * Generates the code for a job and starts it on EC2.
     *
     * @param job The [Job] to run.
     * @return The script id of the script that was started.
     */
    fun startJob(job: Job): Long = IO.fx {
        val modelFile = Paths.get(job.userOldModelPath).toFile()
        val trainModelScriptGenerator = when (
            val model = loadLayersFromHDF5.load(modelFile).bind()) {
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
        userNewModel = model,
        userAuth = null,
        generateDebugComments = job.generateDebugComments
    )
}
