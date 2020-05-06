package edu.wpi.inndie.tfdata

/**
 * A verbosity level for training.
 *
 * @param value The underlying value.
 */
sealed class Verbosity(val value: Int) {

    /**
     * No progress updates.
     */
    object Silent : Verbosity(0)

    /**
     * A progress bar that gets updated as the model is trained.
     */
    object ProgressBar : Verbosity(1)

    /**
     * A new line per epoch. Recommended when not running interactively.
     */
    object OneLinePerEpoch : Verbosity(2)

    override fun toString() = value.toString()
}
