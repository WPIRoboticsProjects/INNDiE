package edu.wpi.axon.aws

import edu.wpi.inndie.db.data.TrainingScriptProgress

interface TrainingScriptProgressReporter {

    /**
     * Queries for the current progress state of the script.
     *
     * @param jobId The id of the Job associated with the script.
     * @return The current progress state of the script.
     */
    fun getTrainingProgress(jobId: Int): TrainingScriptProgress

    /**
     * Overrides the progress reporting so that this value is always returned. Progress reporting
     * must be micromanaged with this method after this is called for the first time (per Job).
     *
     * @param jobId The id of the Job associated with the script.
     * @param progress The new progress to report.
     */
    fun overrideTrainingProgress(jobId: Int, progress: TrainingScriptProgress)
}
