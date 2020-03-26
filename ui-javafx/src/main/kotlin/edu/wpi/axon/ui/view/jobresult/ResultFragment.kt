package edu.wpi.axon.ui.view.jobresult

import java.awt.Desktop
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.chart.NumberAxis
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.layout.Priority
import tornadofx.Fragment
import tornadofx.action
import tornadofx.borderpane
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.hgrow
import tornadofx.imageview
import tornadofx.information
import tornadofx.label
import tornadofx.linechart
import tornadofx.multiseries
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
                    "txt", "log", "py" -> textarea {
                        text = it.file.value.readText()
                        isEditable = false
                        isWrapText = true
                    }

                    "csv" -> linechart(
                        it.filename.substringBeforeLast('.'),
                        x = NumberAxis(),
                        y = NumberAxis()
                    ) {
                        val lines = it.file.value.readLines()
                        val titleLine = lines.first()
                        val dataLines = lines.drop(1).map { it.split(',') }
                        val columnTitles = titleLine.split(',')

                        multiseries(*columnTitles.drop(1).toTypedArray()) {
                            dataLines.forEach {
                                data(
                                    it.first().toDouble(),
                                    *it.drop(1).map { it.toDouble() }.toTypedArray()
                                )
                            }
                        }
                    }

                    "bmp", "gif", "jpg", "jpeg", "png" -> imageview(
                        it.file.value.toPath().toUri().toString()
                    ) {
                        isPreserveRatio = true
                        isSmooth = true
                        isCache = true
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
