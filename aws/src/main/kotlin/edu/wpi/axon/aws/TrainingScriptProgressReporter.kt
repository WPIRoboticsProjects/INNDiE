package edu.wpi.axon.aws

import edu.wpi.axon.db.data.TrainingScriptProgress

interface TrainingScriptProgressReporter {

    /**
     * Queries for the current progress state of the script.
     *
     * @param jobId The id of the Job associated with the script.
     * @return The current progress state of the script.
     */
    fun getTrainingProgress(jobId: Int): TrainingScriptProgress
}
