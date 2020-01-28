package edu.wpi.axon.ui

import arrow.fx.IO
import arrow.fx.extensions.fx
import edu.wpi.axon.db.JobDb
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.TrainingScriptProgress
import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Manages the entire lifecycle of a Job. Starting, tracking progress, cancelling, resuming progress
 * updates between restarts, etc.
 */
class JobLifecycleManager : KoinComponent {

    private val jobRunner by inject<JobRunner>()
    private val jobDb by inject<JobDb>()

    init {
        // TODO: Query the DB for all jobs that were running when Axon was closed and start tracking
        //  them again.
    }

    /**
     * Generates the code for a job and starts it.
     *
     * @param job The [Job] to run.
     * @return An [IO] for continuation.
     */
    fun startJob(job: Job): IO<Unit> = runJob(job)

    /**
     * Cancels the Job. If the Job is currently running, it is interrupted. If the Job is not
     * running, this method does nothing.
     *
     * @param id The id of the Job to cancel.
     */
    fun cancelJob(id: Int): IO<Unit> = jobRunner.cancelJob(id)

    private fun runJob(job: Job) = IO.fx {
        jobDb.update(job.copy(status = TrainingScriptProgress.Creating))

        jobRunner.startJob(job).bind()
        LOGGER.debug { "Started job with id: ${job.id}" }

        effect { delay(defaultWaitAfterStaringJobMs) }.bind()

        jobRunner.waitForFinish(job.id) {
            jobDb.update(job.copy(status = it))
        }.bind()
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
        private const val defaultWaitAfterStaringJobMs = 5000L
    }
}
