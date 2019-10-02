package edu.wpi.axon.tfdata.layer

/**
 * Valid values for the `padding` parameter on Pooling-type layers.
 */
enum class PoolingPadding(val value: String) {
    Valid("valid"), Same("same")
}
