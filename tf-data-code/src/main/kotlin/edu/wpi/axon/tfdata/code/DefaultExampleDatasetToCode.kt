package edu.wpi.axon.tfdata.code

import edu.wpi.axon.tfdata.Dataset

class DefaultExampleDatasetToCode : ExampleDatasetToCode {

    override fun datasetToCode(dataset: Dataset.ExampleDataset) =
        "tf.keras.datasets.${dataset.name}.load_data()"
}
