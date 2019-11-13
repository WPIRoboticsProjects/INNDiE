package edu.wpi.axon.aws

import arrow.fx.IO

interface TrainingScriptRunner {

    /**
     * Start running a training script.
     *
     * @param oldModelName The name of the current model (that will be loaded).
     * @param newModelName The name of the new model (that will be trained and saved).
     * @param scriptContents The contents of the training script.
     * @return The script id used to query about the script during and after training.
     */
    fun startScript(
        oldModelName: String,
        newModelName: String,
        scriptContents: String
    ): IO<Long>

    /**
     * Queries for the current progress state of the script.
     *
     * @param scriptId The id of the script, from [startScript].
     * @return The current progress state of the script.
     */
    fun getTrainingProgress(scriptId: Long): IO<TrainingScriptProgress>
}
