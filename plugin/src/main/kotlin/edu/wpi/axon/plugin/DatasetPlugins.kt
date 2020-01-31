package edu.wpi.axon.plugin

object DatasetPlugins {
    val datasetPassthroughPlugin = Plugin.Official(
        "Dataset Passthrough",
        """
        |def process_dataset(x, y):
        |    return (x, y)
        """.trimMargin()
    )
}
