package edu.wpi.axon.ui.view.jobresult

import java.awt.Desktop
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.layout.Priority
import tornadofx.Fragment
import tornadofx.action
import tornadofx.borderpane
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.hgrow
import tornadofx.information
import tornadofx.label
import tornadofx.objectBinding
import tornadofx.putString
import tornadofx.textarea
import tornadofx.vgrow

/**
 * Visualizes a training or testing result.
 */
class ResultFragment : Fragment() {

    private val copyPathButtonType = ButtonType("Copy Path", ButtonBar.ButtonData.APPLY)

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
                            if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
                                Desktop.getDesktop().browseFileDirectory(it.file.value)
                            } else {
                                information(header = it.filename,
                                        content = it.file.value.path,
                                        buttons = *arrayOf(copyPathButtonType, ButtonType.OK),
                                        title = "Result File Path",
                                        actionFn = { button ->
                                            when (button) {
                                                copyPathButtonType -> clipboard.putString(it.file.value.path)
                                            }
                                        })
                            }
                        }
                    }
                }
            }
        })
    }
}
