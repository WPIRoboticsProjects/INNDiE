package edu.wpi.axon.ui.view

import javafx.beans.property.SimpleLongProperty
import tornadofx.View
import tornadofx.action
import tornadofx.button
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.spinner

class Preferences: View("Preferences") {
    val awsPollingRateProperty = SimpleLongProperty()

    override val root = form {
        fieldset("AWS") {
            field("Polling Rate") {
                spinner(min = 100, max = 60000, initialValue = 1000, amountToStepBy = 1000, editable = true, property = awsPollingRateProperty)
            }
        }
        button("Save") {
            action {
                preferences {
                    putLong("awsPollingRateProperty", awsPollingRateProperty.value)
                }
            }
        }
    }

    init {
        preferences {
            awsPollingRateProperty.set(getLong("awsPollingRateProperty", 1000))
        }
    }
}