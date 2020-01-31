package edu.wpi.axon.plugin

object DatasetPlugins {
    val datasetPassthroughPlugin = Plugin.Official(
        "Dataset Passthrough",
        """
        |def process_dataset(train_x, train_y, test_x, test_y):
        |    return (train_x, train_y, test_x, test_y)
        """.trimMargin()
    )
}
