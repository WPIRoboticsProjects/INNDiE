package edu.wpi.axon.tfdata.layer

/**
 * Valid values for the `data_format` parameter on Pooling-type layers.
 */
enum class PoolingDataFormat(val value: String) {
    ChannelsFirst("channels_first"), ChannelsLast("channels_last")
}
