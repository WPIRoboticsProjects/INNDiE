package edu.wpi.axon.plugin

object DatasetPlugins {
    val datasetPassthroughPlugin = Plugin.Official(
        "Dataset Passthrough",
        """
        |def process_dataset(x, y):
        |    return (x, y)
        """.trimMargin()
    )

    val processMnistTypePlugin = Plugin.Official(
        "Process MNIST-type Dataset",
        """
        |def process_dataset(x, y):
        |    newX = tf.cast(x / 255.0, tf.float32)
        |    newY = tf.cast(y / 255.0, tf.float32)
        |    newX = newX[..., tf.newaxis]
        |    newY = newY[..., tf.newaxis]
        |    return (newX, newY)
        """.trimMargin()
    )
}
