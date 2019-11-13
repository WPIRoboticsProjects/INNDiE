package edu.wpi.axon.tfdata.code

import edu.wpi.axon.tfdata.Dataset

class DefaultDatasetToCode : DatasetToCode {

    override fun datasetToCode(dataset: Dataset) = when (dataset) {
        is Dataset.Custom -> TODO("Custom datasets not implemented yet.")
        is Dataset.ExampleDataset -> "tf.keras.datasets.${dataset.name}"
    }
}
