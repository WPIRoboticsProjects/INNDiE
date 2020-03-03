package edu.wpi.axon.ui.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import edu.wpi.axon.plugin.PluginManager
import edu.wpi.axon.ui.model.PluginManagerModel
import edu.wpi.axon.ui.model.PluginManagerScope
import edu.wpi.axon.ui.model.PluginModel
import edu.wpi.axon.ui.model.PreferencesModel
import edu.wpi.axon.util.datasetPluginManagerName
import edu.wpi.axon.util.testPluginManagerName
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import javafx.scene.layout.Priority
import software.amazon.awssdk.services.ec2.model.InstanceType
import tornadofx.Fragment
import tornadofx.View
import tornadofx.action
import tornadofx.attachTo
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.combobox
import tornadofx.enableWhen
import tornadofx.field
import tornadofx.fieldset
import tornadofx.filterInput
import tornadofx.find
import tornadofx.form
import tornadofx.isLong
import tornadofx.label
import tornadofx.listview
import tornadofx.required
import tornadofx.separator
import tornadofx.textarea
import tornadofx.textfield
import tornadofx.toObservable
import tornadofx.validator
import tornadofx.vbox
import tornadofx.vgrow

class PreferencesView : View("Preferences") {

    private val model by inject<PreferencesModel>()
    private val datasetPluginManager by di<PluginManager>(datasetPluginManagerName)
    private val testPluginManager by di<PluginManager>(testPluginManagerName)

    override val root = vbox {
        form {
            fieldset("AWS") {
                field("Default Training Instance Type") {
                    combobox(model.defaultEC2NodeTypeProperty) {
                        items = InstanceType.knownValues().toList().sorted().toObservable()
                        required()
                    }
                }
                field("Polling Rate (ms)") {
                    textfield(model.statusPollingDelayProperty) {
                        filterInput { it.controlNewText.isLong() }
                        validator { isLongInRange(it, 5000L..60_000L) }
                    }
                }
            }
            buttonbar(ButtonBar.BUTTON_ORDER_NONE) {
                button("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE) {
                    action {
                        model.rollback()
                        close()
                    }
                }
                button("Ok", ButtonBar.ButtonData.OK_DONE) {
                    enableWhen(model.valid)
                    action {
                        model.commit {
                            close()
                        }
                    }
                }
            }
        }
        separator()
        add(find<PluginManagerEditor>(PluginManagerScope(PluginManagerModel().apply { this.item = datasetPluginManager })))
    }
}



class PluginManagerEditor: Fragment() {

    val model by inject<PluginManagerModel>()

    override val root = vbox {
        listview(model.plugins) {
            cellFormat {
                text = it.name
                graphic = button(graphic = FontAwesomeIconView(FontAwesomeIcon.PENCIL)) {
                    setOnAction {
                        find<PluginEditor>().openWindow()
                    }
                }
            }
        }
        button(graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS)) {
            setOnAction {
                find<PluginEditor>().openWindow()
            }
        }
    }
}

class PluginEditor: Fragment() {

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
                button("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE) {
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