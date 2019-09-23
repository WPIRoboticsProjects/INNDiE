package edu.wpi.axon.tfdata.layer

import arrow.core.None
import arrow.core.Option
import arrow.core.Some

/**
 * A TensorFlow layer.
 */
interface Layer {

    /**
     * The unique name of this layer.
     */
    val name: String

    /**
     * Any inputs to this layer. Should be [None] for Sequential models and [Some] for other
     * models. Each element is the [name] of another layer.
     */
    val inputs: Option<Set<String>>
}
