package edu.wpi.axon.ui.view

import tornadofx.Fragment
import tornadofx.action
import tornadofx.borderpane
import tornadofx.bottom
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.center
import tornadofx.label
import tornadofx.removeFromParent
import tornadofx.textarea

/**
 * Visualizes a training or testing result.
 */
class ResultFragment : Fragment() {

    val data: LazyResult by param()

    override val root = borderpane {
        center {
            when (val extension = data.filename.substringAfterLast('.', "")) {
                "txt", "log", "py" -> textarea {
                    text = data.file.value.readText()
                    isEditable = false
                    isWrapText = true
                }

                else -> label("Cannot visualize data format: $extension")
            }
        }

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
    }
}
