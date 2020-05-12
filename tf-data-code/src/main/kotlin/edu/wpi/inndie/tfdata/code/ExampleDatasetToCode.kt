package edu.wpi.inndie.tfdata.code

import edu.wpi.inndie.tfdata.Dataset

interface ExampleDatasetToCode {

    /**
     * Get the code to make a new instance of a [dataset]. This is a code fragment that will become
     * the body of a function which will be called to load the dataset.
     *
     * @param dataset The [Dataset].
     * @return The code to make a new instance of the [dataset].
     */
    fun datasetToCode(dataset: Dataset.ExampleDataset): String
}
