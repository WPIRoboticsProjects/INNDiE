package edu.wpi.axon.ui.view

import tornadofx.Fragment
import tornadofx.borderpane
import tornadofx.center
import tornadofx.label
import tornadofx.textarea
import java.io.File

class ResultFragment : Fragment() {

    val data: File by param()

    override val root = borderpane {
        center {
            when (data.extension) {
                "txt", "log", "py" -> textarea {
                    text = data.readText()
                    isEditable = false
                    isWrapText = true
                }

                else -> label("Cannot visualize data format: ${data.extension}")
            }
        }
    }
}
