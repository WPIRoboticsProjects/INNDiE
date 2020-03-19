package edu.wpi.axon.ui.view.jobresult

import javafx.beans.property.SimpleObjectProperty
import tornadofx.Fragment
import tornadofx.action
import tornadofx.borderpane
import tornadofx.bottom
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.label
import tornadofx.objectBinding
import tornadofx.removeFromParent
import tornadofx.textarea

/**
 * Visualizes a training or testing result.
 */
class ResultFragment : Fragment() {

    val data = SimpleObjectProperty<LazyResult>()

    override val root = borderpane {
        centerProperty().bind(data.objectBinding {
            if (it == null) {
                bottom {
                    buttonbar {
                        button("Close") {
                            action {
                                this@ResultFragment.removeFromParent()
                            }
                        }
                    }
                }

                label("No data.")
            } else {
                bottom {
                    buttonbar {
                        button("Show File") {
                            action {
                                TODO("Show the file in the native file browser.")
                            }
                        }

                        button("Close") {
                            action {
                                this@ResultFragment.removeFromParent()
                            }
                        }
                    }
                }

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
    }
}
