package edu.wpi.axon.ui.model

import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer

sealed class WizardTask(val title: String, val description: String) {

    data class TaskInput(val title: String, val description: String, val datasetType: DatasetType, val dataset: Dataset, val optimizer: Optimizer, val loss: Loss)

    abstract val supportedInputs: List<TaskInput>

    object Classification : WizardTask("Classification", "Separate images into categories") {
        override val supportedInputs: List<TaskInput>
            get() = listOf(
                    TaskInput("MNIST", "Classify handwritten digits", DatasetType.EXAMPLE, Dataset.ExampleDataset.Mnist, Optimizer.Adam(), Loss.SparseCategoricalCrossentropy),
                    TaskInput("Fashion MNIST", "Classify photos of clothing", DatasetType.EXAMPLE, Dataset.ExampleDataset.FashionMnist, Optimizer.Adam(), Loss.SparseCategoricalCrossentropy)
            )
    }

    object Detection : WizardTask("Detection", "Detection objects in images") {
        override val supportedInputs: List<TaskInput>
            get() = listOf(
//                    TaskInput("Custom", "Detect custom objects using a Supervise.ly dataset", DatasetType.CUSTOM, Dataset.Custom("", ""))
            )
    }
}

