package edu.wpi.axon.tflayers

/**
 * A TensorFlow layer.
 */
interface Layer {

    /**
     * The unique name of this layer.
     */
    val name: String
}
