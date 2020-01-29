package edu.wpi.axon.aws

import edu.wpi.axon.db.data.TrainingScriptProgress

interface TrainingScriptRunner {

    /**
     * Start running a training script.
     *
     * @param config The data needed to start the script.
     */
    fun startScript(config: RunTrainingScriptConfiguration)

    /**
     * Queries for the current progress state of the script.
     *
     * @param jobId The id of the Job associated with the script given to [startScript].
     * @return The current progress state of the script.
     */
    fun getTrainingProgress(jobId: Int): TrainingScriptProgress

    /**
     * Cancels the script. If the script is currently running, it is interrupted. If the script is
     * not running, this method does nothing.
     *
     * @param jobId The id of the Job associated with the script given to [startScript].
     */
    fun cancelScript(jobId: Int)
}
