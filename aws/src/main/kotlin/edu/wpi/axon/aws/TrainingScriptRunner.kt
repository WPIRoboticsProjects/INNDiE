package edu.wpi.axon.aws

interface TrainingScriptRunner : TrainingScriptProgressReporter {

    /**
     * Start running a training script.
     *
     * @param config The data needed to start the script.
     */
    fun startScript(config: RunTrainingScriptConfiguration)

    /**
     * Cancels the script. If the script is currently running, it is interrupted. If the script is
     * not running, this method does nothing.
     *
     * @param jobId The id of the Job associated with the script given to [startScript].
     */
    fun cancelScript(jobId: Int)
}
