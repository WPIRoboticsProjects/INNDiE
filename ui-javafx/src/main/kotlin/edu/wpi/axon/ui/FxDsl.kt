package edu.wpi.axon.ui

/*
Field label
Field class
Validation
    - Not null?
    - Must be a number?
        - Number must be in a range?
        - Number must not be in a range?
Field class configuration
    - Cell factory
    - Initial value or selection
    - When to save data

form {
    field("Optimizer") {
        control(
            ComboBox<KClass<out Optimizer>>().apply {
                disableProperty().bind(jobInProgressProperty)
            }
        )
        cells {
            OptimizerCell()
        }
        items(Optimizer::class.sealedSubclasses)
        validation {
            notNull()
        }
        onSave {

        }
    }
}
 */
