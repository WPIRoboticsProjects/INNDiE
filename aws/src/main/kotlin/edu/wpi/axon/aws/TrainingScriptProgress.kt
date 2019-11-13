package edu.wpi.axon.aws

/**
 * The states a training script can be in.
 */
sealed class TrainingScriptProgress {

    /**
     * The script has not been started yet.
     */
    object NotStarted : TrainingScriptProgress()

    /**
     * The training is in progress.
     *
     * @param percentComplete The percent of epochs that have been completed.
     */
    data class InProgress(val percentComplete: Double) : TrainingScriptProgress()

    /**
     * The training is finished.
     */
    object Completed : TrainingScriptProgress()
}
