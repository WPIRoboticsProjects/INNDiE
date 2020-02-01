package edu.wpi.axon.aws

import java.io.File

interface TrainingScriptRunner : TrainingScriptProgressReporter, TrainingScriptCanceller {

    /**
     * Start running a training script.
     *
     * @param config The data needed to start the script.
     */
    fun startScript(config: RunTrainingScriptConfiguration)

    fun listResults(id: Int): List<String>

    fun getResult(id: Int, filename: String): File
}
