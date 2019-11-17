package edu.wpi.axon.training

import arrow.core.NonEmptyList
import arrow.core.Validated
import edu.wpi.axon.tfdata.Model

/**
 * Trains a [Model].
 */
interface TrainModelScriptGenerator<T : Model> {

    /**
     * The train state to pull all the configuration data from.
     */
    val trainState: TrainState<T>

    /**
     * Generates a script that trains a [Model].
     *
     * @return The script or a nel of errors.
     */
    fun generateScript(): Validated<NonEmptyList<String>, String>
}
