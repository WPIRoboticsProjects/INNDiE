package edu.wpi.inndie.plugin

object DatasetPlugins {
    val datasetPassthroughPlugin = Plugin.Official(
        "Dataset Passthrough",
        """
        |def process_dataset(x, y):
        |    return (x, y)
        """.trimMargin()
    )

    val divideByTwoFiveFivePlugin = Plugin.Official(
        "Divide Data Elements by 255",
        """
        |def process_dataset(x, y):
        |    newX = tf.cast(x / 255.0, tf.float32)
        |    return (newX, newY)
        """.trimMargin()
    )

    val processMnistTypePlugin = Plugin.Official(
        "Process MNIST-type Dataset",
        """
        |def process_dataset(x, y):
        |    newX = tf.cast(x / 255.0, tf.float32)
        |    newX = newX[..., tf.newaxis]
        |    newY = tf.keras.utils.to_categorical(y)
        |    return (newX, newY)
        """.trimMargin()
    )

    val processMnistTypeForMobilenetPlugin =
        Plugin.Official(
            "Process MNIST-type for Mobilenet",
            """
        |def process_dataset(x, y):
        |    newX = tf.cast(x / 255.0, tf.float32)
        |    newX = newX[..., tf.newaxis]
        |    newX = tf.image.resize_images(newX, (224, 224), method=tf.image.ResizeMethod.BILINEAR, align_corners=True)
        |    newX = tf.image.grayscale_to_rgb(newX)
        |    newY = tf.keras.utils.to_categorical(y)
        |    return (newX, newY)
        """.trimMargin()
        )
}
