package edu.wpi.axon.ui.model

sealed class WizardTask(val title: String, val description: String) {

    enum class DataType {
        MNIST, SUPERVISELY
    }

    data class TaskInput(val title: String, val description: String, val type: DataType)

    abstract val supportedInputs: List<TaskInput>

    object Classification : WizardTask("Classification", "Separate images into categories") {
        override val supportedInputs: List<TaskInput>
            get() = listOf(
                    TaskInput("MNIST", "Classify handwritten digits", DataType.MNIST),
                    TaskInput("Fashion MNIST", "Classify photos of clothing", DataType.MNIST)
            )
    }

    object Detection : WizardTask("Detection", "Detection objects in images") {
        override val supportedInputs: List<TaskInput>
            get() = listOf(
                    TaskInput("Custom", "Detect custom objects using a Supervise.ly dataset", DataType.SUPERVISELY)
            )
    }
}

