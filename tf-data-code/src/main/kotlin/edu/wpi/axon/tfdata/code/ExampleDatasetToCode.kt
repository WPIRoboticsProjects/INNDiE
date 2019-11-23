package edu.wpi.axon.tfdata.code

import edu.wpi.axon.tfdata.Dataset

interface ExampleDatasetToCode {

    /**
     * Get the code to make a new instance of a [dataset].
     *
     * @param dataset The [Dataset].
     * @return The code to make a new instance of the [dataset].
     */
    fun datasetToCode(dataset: Dataset.ExampleDataset): String
}
