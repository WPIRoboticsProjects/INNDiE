package edu.wpi.axon.aws

interface TrainingScriptRunner : TrainingScriptProgressReporter, TrainingScriptCanceller {

    /**
     * Start running a training script.
     *
     * @param config The data needed to start the script.
     */
    fun startScript(config: RunTrainingScriptConfiguration)
}
