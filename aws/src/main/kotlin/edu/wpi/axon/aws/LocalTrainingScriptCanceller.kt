package edu.wpi.axon.aws

import edu.wpi.axon.db.data.TrainingScriptProgress

/**
 * A [TrainingScriptCanceller] that is designed for an [LocalTrainingScriptRunner].
 */
class LocalTrainingScriptCanceller : TrainingScriptCanceller {

    private val scriptThreadMap = mutableMapOf<Int, Thread>()
    private val updateProgressMap = mutableMapOf<Int, (TrainingScriptProgress) -> Unit>()

    /**
     * Adds a Job.
     *
     * @param jobId The Job ID.
     * @param thread The thread the Job is being trained in. Will be interrupted to cancel the Job.
     * @param updateProgress A callback used to update the progress of the Job.
     */
    fun addJob(jobId: Int, thread: Thread, updateProgress: (TrainingScriptProgress) -> Unit) {
        scriptThreadMap[jobId] = thread
        updateProgressMap[jobId] = updateProgress
    }

    /**
     * Adds a Job that was pulled from the DB after Axon was restarted (so there is no thread data).
     *
     * @param jobId The Job ID.
     * @param updateProgress A callback used to update the progress of the Job.
     */
    fun addJobAfterRestart(
        jobId: Int,
        updateProgress: (TrainingScriptProgress) -> Unit
    ) {
        // TODO: This one doesn't make a lot of sense because there isn't anything to cancel in
        //  here, we just override the progress to Error.
        updateProgressMap[jobId] = updateProgress
    }

    override fun cancelScript(jobId: Int) {
        require(jobId in updateProgressMap.keys)
        scriptThreadMap[jobId]?.interrupt()
        updateProgressMap[jobId]!!(TrainingScriptProgress.Error)
    }
}
