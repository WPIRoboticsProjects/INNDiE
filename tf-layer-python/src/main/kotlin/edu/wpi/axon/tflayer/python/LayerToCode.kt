package edu.wpi.axon.tflayer.python

import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Layer

interface LayerToCode {

    /**
     * Get the code to make a new instance of a [layer].
     *
     * @param layer The [Layer].
     * @return The code to make a new instance of the [layer].
     */
    fun makeNewLayer(layer: Layer): String

    /**
     * Get the code to make an [activation] function.
     *
     * @param activation The [Activation].
     * @return The code for the [activation] function.
     */
    fun makeNewActivation(activation: Activation): String
}
