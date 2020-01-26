package edu.wpi.axon.ui

import arrow.core.Either
import arrow.core.None
import arrow.fx.IO
import arrow.fx.extensions.fx
import edu.wpi.axon.aws.RunTrainingScriptConfiguration
import edu.wpi.axon.aws.TrainingScriptRunner
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.training.TrainGeneralModelScriptGenerator
import edu.wpi.axon.training.TrainSequentialModelScriptGenerator
import edu.wpi.axon.training.TrainState
import kotlinx.coroutines.delay
import org.koin.core.KoinComponent
import org.koin.core.inject

class JobRunner : KoinComponent {

    private val scriptRunner: TrainingScriptRunner by inject()

    /**
     * Generates the code for a job and starts it on EC2.
     *
     * @param job The [Job] to run.
     * @return The script id of the script that was started.
     */
    fun startJob(job: Job): IO<Long> = IO.fx {
        val trainModelScriptGenerator = IO {
            when (val model = job.userModel) {
                is Model.Sequential -> TrainSequentialModelScriptGenerator(toTrainState(job, model))
                is Model.General -> TrainGeneralModelScriptGenerator(toTrainState(job, model))
            }
        }.bind()

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
     * @return An [IO] for continuation.
     */
    fun waitForChange(id: Long, previousState: TrainingScriptProgress): IO<Unit> =
        IO.tailRecM(scriptRunner.getTrainingProgress(id)) {
            IO {
                if (it != previousState) {
                    Either.Right(Unit)
                } else {
                    delay(5000)
                    Either.Left(scriptRunner.getTrainingProgress(id))
                }
            }
        }

    private fun <T : Model> toTrainState(
        job: Job,
        model: T
    ) = TrainState(
        userOldModelPath = job.userOldModelPath,
        userNewModelPath = job.userNewModelName,
        userDataset = job.userDataset,
        userOptimizer = job.userOptimizer,
        userLoss = job.userLoss,
        userMetrics = job.userMetrics,
        userEpochs = job.userEpochs,
        userValidationSplit = None, // TODO: Add this to Job and pull it from there
        userNewModel = model,
        generateDebugComments = job.generateDebugComments
    )
}
