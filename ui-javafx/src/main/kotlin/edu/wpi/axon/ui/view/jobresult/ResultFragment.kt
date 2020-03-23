package edu.wpi.axon.ui.view.jobresult

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import tornadofx.Fragment
import tornadofx.action
import tornadofx.borderpane
import tornadofx.bottom
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.information
import tornadofx.label
import tornadofx.objectBinding
import tornadofx.putString
import tornadofx.textarea
import java.awt.Desktop

/**
 * Visualizes a training or testing result.
 */
class ResultFragment : Fragment() {

    private val copyPathButtonType = ButtonType("Copy Path", ButtonBar.ButtonData.APPLY)

    val data = SimpleObjectProperty<LazyResult>()

    override val root = borderpane {
        centerProperty().bind(data.objectBinding {
            if (it == null) {
                bottom { }
                label("No data.")
            } else {
                bottom {
                    buttonbar {
                        button("Show File Location") {
                            action {
                                if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
                                    Desktop.getDesktop().browseFileDirectory(it.file.value)
                                } else {
                                    information(header = it.filename,
                                            content = it.file.value.path,
                                            buttons = *arrayOf(copyPathButtonType, ButtonType.OK),
                                            title = "Result File Path",
                                            actionFn = { button ->
                                                when(button) {
                                                    copyPathButtonType -> clipboard.putString(it.file.value.path)
                                                }
                                            })
                                }
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
