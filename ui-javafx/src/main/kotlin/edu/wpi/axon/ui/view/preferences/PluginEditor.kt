package edu.wpi.axon.ui.view.preferences

import edu.wpi.axon.ui.model.PluginModel
import javafx.geometry.Orientation
import javafx.scene.control.ButtonBar
import javafx.scene.layout.Priority
import tornadofx.Fragment
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.textarea
import tornadofx.textfield
import tornadofx.vgrow

class PluginEditor : Fragment() {

    val model by inject<PluginModel>()

    override val root = form {
        fieldset("Plugin Editor", labelPosition = Orientation.VERTICAL) {
            field("Name", Orientation.VERTICAL) {
                textfield(model.name) {
                }
            }
            field("Content", Orientation.VERTICAL) {
                textarea(model.contents) {
                    prefRowCount = 5
                    vgrow = Priority.ALWAYS
                }
            }
            buttonbar {
                button("Cancel",
                    ButtonBar.ButtonData.CANCEL_CLOSE
                ) {
                    setOnAction {
                        model.rollback()
                        close()
                    }
                }
                button("Save", ButtonBar.ButtonData.OK_DONE) {
                    setOnAction {
                        model.commit()
                        close()
                    }
                }
            }
        }
    }
}
