package edu.wpi.axon.plugin

object LoadTestDataPlugins {

    // TODO: Don't hardcode which element to return
    val loadExampleDatasetPlugin = Plugin.Official(
        "Load an Example Dataset",
        """
        |def load_dataset(input):
        |import json
        |loaded_json = json.loads(input)
        |
        |try:
        |    type = loaded_json["example_dataset"]
        |    if type == "boston_housing":
        |        dataset = tf.keras.datasets.boston_housing
        |    elif type == "cifar10":
        |        dataset = tf.keras.datasets.cifar10
        |    elif type == "cifar100":
        |        dataset = tf.keras.datasets.cifar100
        |    elif type == "fashion_mnist":
        |        dataset = tf.keras.datasets.fashion_mnist
        |    elif type == "imdb":
        |        dataset = tf.keras.datasets.imdb
        |    elif type == "mnist":
        |        dataset = tf.keras.datasets.mnist
        |    elif type == "reuters":
        |        dataset = tf.keras.datasets.reuters
        |    else:
        |        raise RuntimeError("Cannot load the dataset.")
        |except KeyError:
        |    raise RuntimeError("Cannot load the dataset.")
        |
        |(x_train, y_train), (x_test, y_test) = dataset
        |x_train = tf.cast(x_train / 255, tf.float32)
        |x_train = x_train[..., tf.newaxis]
        |return (x_train, 1)
        """.trimMargin()
    )
}
