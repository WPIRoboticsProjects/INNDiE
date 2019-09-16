package edu.wpi.axon.tfdata.code

import edu.wpi.axon.tfdata.Dataset

class DefaultDatasetToCode : DatasetToCode {

    override fun datasetToCode(dataset: Dataset) = "tf.keras.datasets.${dataset.name}"
}
