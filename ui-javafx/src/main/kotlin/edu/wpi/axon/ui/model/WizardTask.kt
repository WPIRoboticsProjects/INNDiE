package edu.wpi.axon.ui.model

import edu.wpi.axon.tfdata.Dataset

sealed class WizardTask(val title: String, val description: String) {

    data class TaskInput(val title: String, val description: String, val datasetType: DatasetType, val dataset: Dataset)

    abstract val supportedInputs: List<TaskInput>

    object Classification : WizardTask("Classification", "Separate images into categories") {
        override val supportedInputs: List<TaskInput>
            get() = listOf(
                    TaskInput("MNIST", "Classify handwritten digits", DatasetType.EXAMPLE, Dataset.ExampleDataset.Mnist),
                    TaskInput("Fashion MNIST", "Classify photos of clothing", DatasetType.EXAMPLE, Dataset.ExampleDataset.FashionMnist)
            )
    }

    object Detection : WizardTask("Detection", "Detection objects in images") {
        override val supportedInputs: List<TaskInput>
            get() = listOf(
//                    TaskInput("Custom", "Detect custom objects using a Supervise.ly dataset", DatasetType.CUSTOM, Dataset.Custom("", ""))
            )
    }
}

