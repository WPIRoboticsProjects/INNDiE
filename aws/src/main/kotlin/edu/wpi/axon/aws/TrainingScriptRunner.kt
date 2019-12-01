package edu.wpi.axon.aws

import arrow.fx.IO
import edu.wpi.axon.dbdata.TrainingScriptProgress

interface TrainingScriptRunner {

    /**
     * Start running a training script.
     *
     * @param scriptDataForEC2 The data needed to start the script.
     * @return The script id used to query about the script during and after training.
     */
    fun startScript(scriptDataForEC2: ScriptDataForEC2): IO<Long>

    /**
     * Queries for the current progress state of the script.
     *
     * @param scriptId The id of the script, from [startScript].
     * @return The current progress state of the script.
     */
    fun getTrainingProgress(scriptId: Long): IO<TrainingScriptProgress>
}
