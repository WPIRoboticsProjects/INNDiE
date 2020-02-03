package edu.wpi.axon.ui

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.fx.IO
import edu.wpi.axon.aws.EC2Manager
import edu.wpi.axon.aws.EC2TrainingScriptCanceller
import edu.wpi.axon.aws.EC2TrainingScriptProgressReporter
import edu.wpi.axon.aws.EC2TrainingScriptRunner
import edu.wpi.axon.aws.LocalTrainingScriptCanceller
import edu.wpi.axon.aws.LocalTrainingScriptProgressReporter
import edu.wpi.axon.aws.LocalTrainingScriptRunner
import edu.wpi.axon.aws.RunTrainingScriptConfiguration
import edu.wpi.axon.aws.S3Manager
import edu.wpi.axon.aws.TrainingScriptCanceller
import edu.wpi.axon.aws.TrainingScriptProgressReporter
import edu.wpi.axon.aws.TrainingScriptRunner
import edu.wpi.axon.aws.preferences.PreferencesManager
import edu.wpi.axon.db.data.Job
import edu.wpi.axon.db.data.JobTrainingMethod
import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tflayerloader.ModelLoaderFactory
import edu.wpi.axon.training.TrainGeneralModelScriptGenerator
import edu.wpi.axon.training.TrainSequentialModelScriptGenerator
import edu.wpi.axon.training.TrainState
import edu.wpi.axon.util.FilePath
import edu.wpi.axon.util.axonBucketName
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlinx.coroutines.delay
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named

/**
 * Handles running, cancelling, and progress polling for Jobs.
 */
internal class JobRunner : KoinComponent {

    private val runners = mutableMapOf<Int, TrainingScriptRunner>()
    private val progressReporters = mutableMapOf<Int, TrainingScriptProgressReporter>()
    private val cancellers = mutableMapOf<Int, TrainingScriptCanceller>()

    /**
     * Generates the code for a job and starts it.
     *
     * @param job The [Job] to run.
     * @return An [IO] for continuation.
     */
    internal fun startJob(job: Job): JobTrainingMethod {
        // Verify that we can start the job. No sense in starting a Job that is already running.
        require(
            job.status == TrainingScriptProgress.NotStarted ||
                job.status == TrainingScriptProgress.Completed ||
                job.status == TrainingScriptProgress.Error
        ) {
            "The Job to start must not be running. Got a Job with state: ${job.status}"
        }

        // Verify that usesAWS is configured correctly
        val usesAWS = job.usesAWS
        require(usesAWS is Some) {
            "Cannot start a Job that is configured incorrectly (usesAWS failed)."
        }

        // TODO: Loading the old model will need to be done earlier by another part of Axon. This
        //  code is temporary.
        // If the model to start training from is in S3, we need to download it
        val localOldModel = when (job.userOldModelPath) {
            is FilePath.S3 -> {
                val bucketName: Option<String> = get(named(axonBucketName))
                check(bucketName is Some)

                val s3Manager = S3Manager(bucketName.t)
                s3Manager.downloadUntrainedModel(job.userOldModelPath.path)
            }

            is FilePath.Local -> File(job.userOldModelPath.path)
        }

        val oldModel = ModelLoaderFactory().createModelLoader(localOldModel.name)
            .load(localOldModel)
            .unsafeRunSync()

        val workingDir = if (usesAWS.t) {
            // The EC2 runner needs to output to a relative directory (and NOT just the current
            // directory) because it runs the training script in a Docker container and maps the
            // current directory with `-v`.
            Paths.get(".").resolve("output")
        } else {
            // The local runner can work out of any directory.
            localScriptRunnerCache.resolve(job.id.toString()).apply { toFile().mkdirs() }
        }

        val trainModelScriptGenerator = when (job.userNewModel) {
            is Model.Sequential -> {
                require(oldModel is Model.Sequential)
                TrainSequentialModelScriptGenerator(toTrainState(job, workingDir), oldModel)
            }
            is Model.General -> {
                require(oldModel is Model.General)
                TrainGeneralModelScriptGenerator(toTrainState(job, workingDir), oldModel)
            }
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
        ).unsafeRunSync()

        val config = createTrainingScriptConfiguration(job, script, workingDir)

        // Create the correct TrainingScriptRunner based on whether the Job needs to use AWS
        val (scriptRunner, trainingMethod) = if (usesAWS.t) {
            val runner = EC2TrainingScriptRunner(
                // TODO: Allow overriding the default node type in the Job editor form
                get<PreferencesManager>().get().defaultEC2NodeType,
                EC2Manager(),
                S3Manager(getBucket())
            )
            runner.startScript(config)
            runner to JobTrainingMethod.EC2(runner.getInstanceId(config.id))
        } else {
            val runner = LocalTrainingScriptRunner()
            runner.startScript(config)
            runner to JobTrainingMethod.Local
        }

        runners[job.id] = scriptRunner
        progressReporters[job.id] = scriptRunner
        cancellers[job.id] = scriptRunner
        return trainingMethod
    }

    /**
     * Cancels the Job. If the Job is currently running, it is interrupted. If the Job is not
     * running, this method does nothing.
     *
     * @param id The id of the Job to cancel.
     */
    internal fun cancelJob(id: Int) {
        cancellers[id]!!.cancelScript(id)
    }

    internal fun listResults(id: Int) = runners[id]!!.listResults(id)

    internal fun getResult(id: Int, filename: String) = runners[id]!!.getResult(id, filename)

    /**
     * Waits until the [TrainingScriptProgress] state is either completed or error.
     *
     * @param id The Job id.
     * @param progressUpdate A callback that is given the current [TrainingScriptProgress] state
     * every time it is polled.
     * @return An [IO] for continuation.
     */
    internal suspend fun waitForFinish(id: Int, progressUpdate: (TrainingScriptProgress) -> Unit) {
        // Get the latest statusPollingDelay in case the user changed it
        val statusPollingDelay = get<PreferencesManager>().get().statusPollingDelay
        while (true) {
            // Only access `progressReporters` in here, not `runners`
            val progress = progressReporters[id]!!.getTrainingProgress(id)
            progressUpdate(progress)
            if (progress == TrainingScriptProgress.Completed ||
                progress == TrainingScriptProgress.Error
            ) {
                break
            } else {
                delay(statusPollingDelay)
            }
        }
    }

    /**
     * Starts progress reporting after Axon has been restarted. After calling this, it's safe to
     * call [waitForFinish] to resume tracking progress updates for a Job.
     *
     * @param job The Job.
     */
    internal fun startProgressReporting(job: Job) {
        require(runners[job.id] == null)

        // The script contents don't matter to the progress reporter. Set an empty
        // string to avoid having to regenerate the script.
        val config = createTrainingScriptConfiguration(job, "", Files.createTempDirectory(""))

        when (val trainingMethod = job.trainingMethod) {
            is JobTrainingMethod.EC2 -> {
                progressReporters[job.id] = EC2TrainingScriptProgressReporter(
                    EC2Manager(),
                    S3Manager(getBucket())
                ).apply {
                    addJob(config, trainingMethod.instanceId)
                }

                cancellers[job.id] = EC2TrainingScriptCanceller(EC2Manager()).apply {
                    addJob(job.id, trainingMethod.instanceId)
                }
            }

            is JobTrainingMethod.Local -> {
                progressReporters[job.id] = LocalTrainingScriptProgressReporter().apply {
                    addJobAfterRestart(config)
                }

                cancellers[job.id] = LocalTrainingScriptCanceller().apply {
                    addJobAfterRestart(job.id) {
                        progressReporters[job.id]!!.overrideTrainingProgress(job.id, it)
                    }
                }
            }

            is JobTrainingMethod.Untrained ->
                error("Cannot resume training for a Job that has not started training.")
        }
    }

    private fun getBucket(): String {
        val bucket = get<Option<String>>(named(axonBucketName))
        check(bucket is Some) {
            "Tried to create an EC2TrainingScriptRunner but did not have a bucket configured."
        }
        return bucket.t
    }

    private fun createTrainingScriptConfiguration(
        job: Job,
        script: String,
        workingDir: Path
    ) = RunTrainingScriptConfiguration(
        oldModelName = job.userOldModelPath,
        dataset = job.userDataset,
        scriptContents = script,
        epochs = job.userEpochs,
        workingDir = workingDir,
        id = job.id
    )

    @Suppress("UNCHECKED_CAST")
    private fun <T : Model> toTrainState(job: Job, workingDir: Path): TrainState<T> = TrainState(
        userOldModelPath = job.userOldModelPath,
        userDataset = job.userDataset,
        userOptimizer = job.userOptimizer,
        userLoss = job.userLoss,
        userMetrics = job.userMetrics,
        userEpochs = job.userEpochs,
        // TODO: Add userValidationSplit to Job and pull it from there so that the user can
        //  configure it
        userValidationSplit = None,
        userNewModel = job.userNewModel as T,
        generateDebugComments = job.generateDebugComments,
        target = job.target,
        workingDir = workingDir,
        datasetPlugin = job.datasetPlugin,
        jobId = job.id
    )
}
