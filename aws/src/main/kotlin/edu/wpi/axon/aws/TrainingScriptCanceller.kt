package edu.wpi.axon.aws

interface TrainingScriptCanceller {

    /**
     * Cancels the script. If the script is currently running, it is interrupted. If the script is
     * not running, this method does nothing.
     *
     * @param jobId The id of the Job associated with the script.
     */
    fun cancelScript(jobId: Int)
}
