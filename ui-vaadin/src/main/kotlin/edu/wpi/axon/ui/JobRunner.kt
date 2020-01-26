package edu.wpi.axon.ui

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.fx.IO
import arrow.fx.extensions.fx
import edu.wpi.axon.aws.RunTrainingScriptConfiguration
import edu.wpi.axon.aws.S3Manager
import edu.wpi.axon.aws.TrainingScriptRunner
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
import org.koin.core.inject
import org.koin.core.qualifier.named

class JobRunner : KoinComponent {

    private val scriptRunner: TrainingScriptRunner by inject()

    /**
     * Generates the code for a job and starts it on EC2.
     *
     * @param job The [Job] to run.
     * @return The script id of the script that was started.
     */
    fun startJob(job: Job): IO<Long> = IO.fx {
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

        scriptRunner.startScript(
            RunTrainingScriptConfiguration(
                oldModelName = job.userOldModelPath,
                newModelName = job.userNewModelName,
                dataset = job.userDataset,
                scriptContents = script,
                epochs = job.userEpochs
            )
        )
    }

    /**
     * Waits until the [TrainingScriptProgress] state is either completed or error.
     *
     * @param id The script id.
     * @param progressUpdate A callback that is given the current [TrainingScriptProgress] state
     * every time it is polled.
     * @return An [IO] for continuation.
     */
    fun waitForCompleted(id: Long, progressUpdate: (TrainingScriptProgress) -> Unit): IO<Unit> =
        IO.tailRecM(scriptRunner.getTrainingProgress(id)) {
            IO {
                progressUpdate(it)
                if (it == TrainingScriptProgress.Completed || it == TrainingScriptProgress.Error) {
                    Either.Right(Unit)
                } else {
                    delay(5000)
                    Either.Left(scriptRunner.getTrainingProgress(id))
                }
            }
        }

    /**
     * Waits until the [TrainingScriptProgress] state changes.
     *
     * @param id The script id.
     * @param previousState The state to watch for a change from.
     * @return The new [TrainingScriptProgress].
     */
    fun waitForChange(
        id: Long,
        previousState: TrainingScriptProgress
    ): IO<TrainingScriptProgress> = IO.tailRecM(scriptRunner.getTrainingProgress(id)) {
        IO {
            if (it != previousState) {
                Either.Right(it)
            } else {
                delay(5000)
                Either.Left(scriptRunner.getTrainingProgress(id))
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
        generateDebugComments = job.generateDebugComments
    )
}
