package edu.wpi.axon.tflayers

import arrow.core.Option
import arrow.core.Tuple2

sealed class Layer(open val name: String, open val trainable: Boolean) {

    /**
     * A 2D convolutional layer.
     */
    data class Conv2D(
        override val name: String,
        override val trainable: Boolean,
        val filters: Int,
        val kernelSize: Tuple2<Int, Int>,
        val strides: Tuple2<Int, Int>,
        val activation: Activation,
        val batchInputShape: Option<List<Option<Int>>>
    ) : Layer(name, trainable)
}
