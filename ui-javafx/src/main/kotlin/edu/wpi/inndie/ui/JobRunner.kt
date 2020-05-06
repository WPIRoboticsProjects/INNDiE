package edu.wpi.inndie.ui

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.fx.IO
import edu.wpi.inndie.aws.EC2Manager
import edu.wpi.inndie.aws.EC2TrainingResultSupplier
import edu.wpi.inndie.aws.EC2TrainingScriptCanceller
import edu.wpi.inndie.aws.EC2TrainingScriptProgressReporter
import edu.wpi.inndie.aws.EC2TrainingScriptRunner
import edu.wpi.inndie.aws.LocalTrainingResultSupplier
import edu.wpi.inndie.aws.LocalTrainingScriptCanceller
import edu.wpi.inndie.aws.LocalTrainingScriptProgressReporter
import edu.wpi.inndie.aws.LocalTrainingScriptRunner
import edu.wpi.inndie.aws.RunTrainingScriptConfiguration
import edu.wpi.inndie.aws.S3Manager
import edu.wpi.inndie.aws.TrainingResultSupplier
import edu.wpi.inndie.aws.TrainingScriptCanceller
import edu.wpi.inndie.aws.TrainingScriptProgressReporter
import edu.wpi.inndie.aws.TrainingScriptRunner
import edu.wpi.inndie.aws.preferences.PreferencesManager
import edu.wpi.inndie.db.JobDb
import edu.wpi.inndie.db.data.DesiredJobTrainingMethod
import edu.wpi.inndie.db.data.InternalJobTrainingMethod
import edu.wpi.inndie.db.data.Job
import edu.wpi.inndie.db.data.ModelSource
import edu.wpi.inndie.db.data.TrainingScriptProgress
import edu.wpi.inndie.tfdata.Model
import edu.wpi.inndie.training.TrainGeneralModelScriptGenerator
import edu.wpi.inndie.training.TrainSequentialModelScriptGenerator
import edu.wpi.inndie.training.TrainState
import edu.wpi.inndie.util.FilePath
import edu.wpi.inndie.util.axonBucketName
import edu.wpi.inndie.util.getLocalTrainingScriptRunnerWorkingDir
import java.nio.file.Path
import java.nio.file.Paths
import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.qualifier.named

/**
 * Handles running, cancelling, and progress polling for Jobs.
 */
internal class JobRunner : KoinComponent {

    private val runners = mutableMapOf<Int, TrainingScriptRunner>()
    private val progressReporters = mutableMapOf<Int, TrainingScriptProgressReporter>()
    private val cancellers = mutableMapOf<Int, TrainingScriptCanceller>()
    private val resultSuppliers = mutableMapOf<Int, TrainingResultSupplier>()
    private val bucketName by inject<Option<String>>(named(axonBucketName))
    private val modelManager by inject<ModelManager>()
    private val jobDb by inject<JobDb>()

    /**
     * Generates the code for a job and starts it.
     *
     * @param job The [Job] to run.
     * @return An [IO] for continuation.
     */
    internal fun startJob(
        job: Job,
        desiredJobTrainingMethod: DesiredJobTrainingMethod
    ): InternalJobTrainingMethod {
        // Verify that we can start the job. No sense in starting a Job that is already running.
        require(
            job.status == TrainingScriptProgress.NotStarted ||
                job.status == TrainingScriptProgress.Completed ||
                job.status is TrainingScriptProgress.Error
        ) {
            "The Job to start must not be running. Got a Job with state: ${job.status}"
        }

        return when (desiredJobTrainingMethod) {
            DesiredJobTrainingMethod.LOCAL -> {
                val localFile = modelManager.downloadModel(job.userOldModelPath)
                startLocalJob(job.copy(userOldModelPath = ModelSource.FromFile(localFile)))
                InternalJobTrainingMethod.Local
            }

            DesiredJobTrainingMethod.EC2 -> {
                check(bucketName is Some)
                val s3File = modelManager.uploadModel(job.userOldModelPath)
                startEC2Job(job.copy(userOldModelPath = ModelSource.FromFile(s3File)))
            }
        }
    }

    private fun startLocalJob(job: Job) {
        val workingDir =
            getLocalTrainingScriptRunnerWorkingDir(job.id)
        val config = generateScriptAndCreateConfig(job, workingDir)

        val scriptRunner = LocalTrainingScriptRunner().apply { startScript(config) }
        setScriptRunner(job.id, scriptRunner)
    }

    private fun startEC2Job(job: Job): InternalJobTrainingMethod.EC2 {
        // The EC2 runner needs to output to a relative directory (and NOT just the current
        // directory) because it runs the training script in a Docker container and maps the
        // current directory with `-v`.
        val workingDir = Paths.get(".").resolve("output")

        val config = generateScriptAndCreateConfig(job, workingDir)

        val scriptRunner = EC2TrainingScriptRunner(
            // TODO: Allow overriding the default node type in the Job editor form
            get<PreferencesManager>().get().defaultEC2NodeType,
            EC2Manager(),
            S3Manager(getBucket())
        ).apply { startScript(config) }

        setScriptRunner(job.id, scriptRunner)
        return InternalJobTrainingMethod.EC2(scriptRunner.getInstanceId(config.id))
    }

    private fun setScriptRunner(jobId: Int, scriptRunner: TrainingScriptRunner) {
        runners[jobId] = scriptRunner
        progressReporters[jobId] = scriptRunner
        cancellers[jobId] = scriptRunner
        resultSuppliers[jobId] = scriptRunner
    }

    private fun generateScriptAndCreateConfig(
        job: Job,
        workingDir: Path
    ): RunTrainingScriptConfiguration {
        require(job.userOldModelPath is ModelSource.FromFile)
        val modelPath = (job.userOldModelPath as ModelSource.FromFile).filePath
        val oldModel = modelManager.loadModel(job.userOldModelPath)

        val trainModelScriptGenerator = when (job.userNewModel) {
            is Model.Sequential -> {
                require(oldModel is Model.Sequential)
                TrainSequentialModelScriptGenerator(
                    toTrainState(job, modelPath, workingDir),
                    oldModel
                )
            }

            is Model.General -> {
                require(oldModel is Model.General)
                TrainGeneralModelScriptGenerator(toTrainState(job, modelPath, workingDir), oldModel)
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

        return createRunTrainingScriptConfiguration(job, modelPath, script, workingDir)
    }

    /**
     * Cancels the Job. If the Job is currently running, it is interrupted. If the Job is not
     * running, this method does nothing.
     *
     * @param id The id of the Job to cancel.
     */
    internal fun cancelJob(id: Int): Boolean =
        cancellers[id]?.let { it.cancelScript(id); true } ?: false

    /**
     * Get the previously set TrainingResultSupplier or try to create a new one.
     */
    private fun getResultSupplier(id: Int): TrainingResultSupplier? {
        val supplier = resultSuppliers[id]
        if (supplier != null) {
            return supplier
        }

        val job = jobDb.getById(id)
        return if (job == null) {
            null
        } else {
            val newSupplier = when (job.internalTrainingMethod) {
                is InternalJobTrainingMethod.EC2 -> EC2TrainingResultSupplier(
                    S3Manager(getBucket())
                )
                is InternalJobTrainingMethod.Local -> LocalTrainingResultSupplier().apply {
                    addJob(id,
                        getLocalTrainingScriptRunnerWorkingDir(
                            id
                        )
                    )
                }
                else -> null
            }

            newSupplier?.let { resultSuppliers[id] = it }

            newSupplier
        }
    }

    internal fun listResults(id: Int) = getResultSupplier(id)?.listResults(id) ?: emptyList()

    internal fun getResult(id: Int, filename: String) =
        getResultSupplier(id)!!.getResult(id, filename)

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
                progress is TrainingScriptProgress.Error
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

        when (val trainingMethod = job.internalTrainingMethod) {
            is InternalJobTrainingMethod.EC2 -> {
                val ec2Manager = EC2Manager()
                val s3Manager = S3Manager(getBucket())
                progressReporters[job.id] =
                    EC2TrainingScriptProgressReporter(
                        ec2Manager,
                        s3Manager
                    ).apply {
                        addJob(job.id, trainingMethod.instanceId, job.userEpochs)
                    }

                cancellers[job.id] = EC2TrainingScriptCanceller(
                    ec2Manager
                ).apply {
                    addJob(job.id, trainingMethod.instanceId)
                }

                resultSuppliers[job.id] =
                    EC2TrainingResultSupplier(s3Manager)
            }

            is InternalJobTrainingMethod.Local -> {
                // The script contents don't matter to the progress reporter. Set an empty
                // string to avoid having to regenerate the script. The model path doesn't matter
                // either.
                val config = createRunTrainingScriptConfiguration(
                    job,
                    FilePath.Local(""),
                    "",
                    getLocalTrainingScriptRunnerWorkingDir(
                        job.id
                    )
                )

                progressReporters[job.id] = LocalTrainingScriptProgressReporter().apply {
                    addJobAfterRestart(config)
                }

                cancellers[job.id] = LocalTrainingScriptCanceller().apply {
                    addJobAfterRestart(job.id) {
                        progressReporters[job.id]!!.overrideTrainingProgress(job.id, it)
                    }
                }

                resultSuppliers[job.id] = LocalTrainingResultSupplier().apply {
                    addJob(job.id, config.workingDir)
                }
            }

            is InternalJobTrainingMethod.Untrained ->
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

    private fun createRunTrainingScriptConfiguration(
        job: Job,
        modelPath: FilePath,
        script: String,
        workingDir: Path
    ) = RunTrainingScriptConfiguration(
        oldModelName = modelPath,
        dataset = job.userDataset,
        scriptContents = script,
        epochs = job.userEpochs,
        workingDir = workingDir,
        id = job.id
    )

    @Suppress("UNCHECKED_CAST")
    private fun <T : Model> toTrainState(
        job: Job,
        modelPath: FilePath,
        workingDir: Path
    ): TrainState<T> =
        TrainState(
            userOldModelPath = modelPath,
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

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
