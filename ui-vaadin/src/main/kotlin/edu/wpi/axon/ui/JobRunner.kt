package edu.wpi.axon.ui

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.fx.IO
import arrow.fx.extensions.fx
import edu.wpi.axon.aws.EC2Manager
import edu.wpi.axon.aws.EC2TrainingScriptRunner
import edu.wpi.axon.aws.LocalTrainingScriptRunner
import edu.wpi.axon.aws.RunTrainingScriptConfiguration
import edu.wpi.axon.aws.S3Manager
import edu.wpi.axon.aws.TrainingScriptRunner
import edu.wpi.axon.aws.preferences.PreferencesManager
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tflayerloader.ModelLoaderFactory
import edu.wpi.axon.training.TrainGeneralModelScriptGenerator
import edu.wpi.axon.training.TrainSequentialModelScriptGenerator
import edu.wpi.axon.training.TrainState
import edu.wpi.axon.util.FilePath
import edu.wpi.axon.util.axonBucketName
import java.io.File
import kotlinx.coroutines.delay
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named

/**
 * Handles running, cancelling, and progress polling for Jobs.
 */
class JobRunner : KoinComponent {

    private val runners = mutableMapOf<Int, TrainingScriptRunner>()

    /**
     * Generates the code for a job and starts it.
     *
     * @param job The [Job] to run.
     * @return An [IO] for continuation.
     */
    @Suppress("ThrowableNotThrown")
    fun startJob(job: Job): IO<Unit> = IO.fx {
        // Verify that we can start the job
        runners[job.id]?.let {
            when (it.getTrainingProgress(job.id)) {
                // These states are ok
                TrainingScriptProgress.NotStarted,
                TrainingScriptProgress.Completed,
                TrainingScriptProgress.Error -> Unit

                // Any other states are not allowed
                else -> IO.raiseError<Unit>(
                    IllegalArgumentException("Cannot start a job that is currently running.")
                ).bind()
            }
        }

        // Verify that usesAWS is configured correctly
        val jobUsesAWS = job.usesAWS.fold(
            {
                IO.raiseError<Boolean>(
                    IllegalArgumentException(
                        "Cannot start a Job that is configured incorrectly (usesAWS failed)."
                    )
                ).bind()
            },
            { it }
        )

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
            .bind()

        val trainModelScriptGenerator = when (job.userNewModel) {
            is Model.Sequential -> {
                require(oldModel is Model.Sequential)
                TrainSequentialModelScriptGenerator(toTrainState(job), oldModel)
            }
            is Model.General -> {
                require(oldModel is Model.General)
                TrainGeneralModelScriptGenerator(toTrainState(job), oldModel)
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
        ).bind()

        // Create the correct TrainingScriptRunner based on whether the Job needs to use AWS
        val scriptRunner = if (jobUsesAWS) {
            val bucket = get<Option<String>>(named(axonBucketName)).fold(
                {
                    IO.raiseError<String>(
                        IllegalStateException(
                            "Tried to create an EC2TrainingScriptRunner but did not have a " +
                                "bucket configured."
                        )
                    ).bind()
                },
                { it }
            )

            EC2TrainingScriptRunner(
                // TODO: Allow overriding the default node type in the Job editor form
                get<PreferencesManager>().get().defaultEC2NodeType,
                EC2Manager(),
                S3Manager(bucket)
            )
        } else {
            LocalTrainingScriptRunner()
        }

        runners[job.id] = scriptRunner
        scriptRunner.startScript(
            RunTrainingScriptConfiguration(
                oldModelName = job.userOldModelPath,
                newModelName = job.userNewModelName,
                dataset = job.userDataset,
                scriptContents = script,
                epochs = job.userEpochs,
                id = job.id
            )
        )
    }

    /**
     * Cancels the Job. If the Job is currently running, it is interrupted. If the Job is not
     * running, this method does nothing.
     *
     * @param id The id of the Job to cancel.
     */
    fun cancelJob(id: Int): IO<Unit> = IO {
        runners[id]!!.cancelScript(id)
    }

    /**
     * Waits until the [TrainingScriptProgress] state is either completed or error.
     *
     * @param id The Job id.
     * @param progressUpdate A callback that is given the current [TrainingScriptProgress] state
     * every time it is polled.
     * @return An [IO] for continuation.
     */
    fun waitForFinish(id: Int, progressUpdate: (TrainingScriptProgress) -> Unit): IO<Unit> {
        // Get the latest statusPollingDelay in case the user changed it
        val statusPollingDelay = get<PreferencesManager>().get().statusPollingDelay
        return IO.tailRecM(runners[id]!!.getTrainingProgress(id)) {
            IO {
                progressUpdate(it)
                if (it == TrainingScriptProgress.Completed || it == TrainingScriptProgress.Error) {
                    Either.Right(Unit)
                } else {
                    delay(statusPollingDelay)
                    Either.Left(runners[id]!!.getTrainingProgress(id))
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Model> toTrainState(job: Job): TrainState<T> = TrainState(
        userOldModelPath = job.userOldModelPath,
        userNewModelPath = job.userNewModelName,
        userDataset = job.userDataset,
        userOptimizer = job.userOptimizer,
        userLoss = job.userLoss,
        userMetrics = job.userMetrics,
        userEpochs = job.userEpochs,
        userValidationSplit = None, // TODO: Add this to Job and pull it from there
        userNewModel = job.userNewModel as T,
        generateDebugComments = job.generateDebugComments,
        jobId = job.id
    )
}
