package edu.wpi.axon.ui.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.ui.controller.DatasetPluginStore
import edu.wpi.axon.ui.controller.LoadTestDataPluginStore
import edu.wpi.axon.ui.controller.PluginStore
import edu.wpi.axon.ui.controller.ProcessTestOutputPluginStore
import edu.wpi.axon.ui.model.PluginModel
import edu.wpi.axon.ui.model.PreferencesModel
import javafx.geometry.Orientation
import javafx.scene.control.ButtonBar
import javafx.scene.layout.Priority
import software.amazon.awssdk.services.ec2.model.InstanceType
import tornadofx.FX
import tornadofx.Fragment
import tornadofx.Scope
import tornadofx.View
import tornadofx.action
import tornadofx.bindSelected
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.combobox
import tornadofx.enableWhen
import tornadofx.field
import tornadofx.fieldset
import tornadofx.filterInput
import tornadofx.find
import tornadofx.form
import tornadofx.hbox
import tornadofx.isLong
import tornadofx.listview
import tornadofx.onUserSelect
import tornadofx.required
import tornadofx.textarea
import tornadofx.textfield
import tornadofx.toObservable
import tornadofx.validator
import tornadofx.vbox
import tornadofx.vgrow

class PreferencesView : View("Preferences") {

    private val model by inject<PreferencesModel>()
    private val datasetPluginStore by inject<DatasetPluginStore>()
    private val loadTestDataPluginManager by inject<LoadTestDataPluginStore>()
    private val processTestOutputPluginManager by inject<ProcessTestOutputPluginStore>()

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
                        validator { it.isLongInRange(5000L..60_000L) }
                    }
                }
            }
            fieldset("Plugins", labelPosition = Orientation.VERTICAL) {
                field("Dataset") {
                    val scope = Scope()
                    FX.getComponents(scope)[PluginStore::class] = datasetPluginStore
                    add(find<PluginManagerEditor>(scope))
                }
                field("Load Test Data") {
                    val scope = Scope()
                    FX.getComponents(scope)[PluginStore::class] = loadTestDataPluginManager
                    add(find<PluginManagerEditor>(scope))
                }
                field("Process Test Output") {
                    val scope = Scope()
                    FX.getComponents(scope)[PluginStore::class] = processTestOutputPluginManager
                    add(find<PluginManagerEditor>(scope))
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
    }
}

class PluginManagerEditor : Fragment() {

    private val selected by inject<PluginModel>()
    private val store by inject<PluginStore>()

    override val root = vbox {
        listview(store.plugins) {
            bindSelected(selected)
            cellFormat {
                text = it.name
            }
            onUserSelect {
                find<PluginEditor>().openWindow()
            }
            vgrow = Priority.NEVER
            prefHeight = -1.0
            maxHeight = 150.0
        }
        hbox {
            button(graphic = FontAwesomeIconView(FontAwesomeIcon.MINUS)) {
                enableWhen { selected.empty.not().and(selected.unofficial) }
                setOnAction {
                    store.removePlugin(selected.item as Plugin.Unofficial)
                }
            }
            button(graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS)) {
                setOnAction {
                    selected.item = null
                    find<PluginEditor>().openWindow()
                }
            }
        }
    }
}

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
