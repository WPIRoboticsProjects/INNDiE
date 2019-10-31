package edu.wpi.axon.tfdata.layer

/**
 * Valid values for the `data_format` parameter on Pooling-type and Flatten layers.
 */
enum class DataFormat(val value: String) {
    ChannelsFirst("channels_first"), ChannelsLast("channels_last")
}
