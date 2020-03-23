package edu.wpi.axon.ui.view.jobresult

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Priority
import tornadofx.Fragment
import tornadofx.action
import tornadofx.borderpane
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.hgrow
import tornadofx.label
import tornadofx.objectBinding
import tornadofx.textarea
import tornadofx.vgrow

/**
 * Visualizes a training or testing result.
 */
class ResultFragment : Fragment() {

    val data = SimpleObjectProperty<LazyResult>()

    override val root = borderpane {
        centerProperty().bind(data.objectBinding {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS

            if (it == null) {
                label("No data.")
            } else {
                when (val extension = it.filename.substringAfterLast('.', "")) {
                    "txt", "log", "py", "csv" -> textarea {
                        text = it.file.value.readText()
                        isEditable = false
                        isWrapText = true
                    }

                    else -> label("Cannot visualize data format: $extension")
                }
            }
        })

        bottomProperty().bind(data.objectBinding {
            if (it == null) {
                null
            } else {
                buttonbar {
                    button("Show File") {
                        action {
                            TODO("Show the file in the native file browser.")
                        }
                    }
                }
            }
        })
    }
}
