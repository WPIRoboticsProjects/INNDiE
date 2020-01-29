package edu.wpi.axon.ui

import edu.wpi.axon.db.JobDb
import edu.wpi.axon.db.data.Job
import edu.wpi.axon.db.data.TrainingScriptProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.koin.core.KoinComponent

/**
 * Manages the entire lifecycle of a Job. Starting, tracking progress, cancelling, resuming progress
 * updates between restarts, etc.
 */
class JobLifecycleManager internal constructor(
    private val jobRunner: JobRunner,
    private val jobDb: JobDb,
    private val waitAfterStartingJobMs: Long
) : KoinComponent {

    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Synchronizes the state of this manager with the database. Resumes progress tracking for any
     * Jobs that were running when Axon was last closed.
     */
    fun initialize() {
        val runningJobs = jobDb.fetchRunningJobs()
        runningJobs.forEach { job ->
            scope.launch {
                LOGGER.info { "launched" }
                jobRunner.startProgressReporting(job)
                jobRunner.waitForFinish(job.id) {
                    LOGGER.info { "update $it" }
                    jobDb.update(job.id, status = it)
                }
            }
        }
    }

    /**
     * Generates the code for a job and starts it.
     *
     * @param job The [Job] to run.
     */
    fun startJob(job: Job) {
        scope.launch {
            jobDb.update(job.id, status = TrainingScriptProgress.Creating)

            jobRunner.startJob(job)
            LOGGER.debug { "Started job with id: ${job.id}" }

            delay(waitAfterStartingJobMs)

            jobRunner.waitForFinish(job.id) {
                jobDb.update(job.id, status = it)
            }
        }
    }

    /**
     * Cancels the Job. If the Job is currently running, it is interrupted. If the Job is not
     * running, this method does nothing.
     *
     * @param id The id of the Job to cancel.
     */
    fun cancelJob(id: Int) = jobRunner.cancelJob(id)

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
