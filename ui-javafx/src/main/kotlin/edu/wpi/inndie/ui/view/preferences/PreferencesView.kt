package edu.wpi.inndie.ui.view.preferences

import edu.wpi.axon.ui.model.PreferencesModel
import edu.wpi.axon.ui.view.isLongInRange
import edu.wpi.inndie.ui.controller.DatasetPluginStore
import edu.wpi.inndie.ui.controller.LoadTestDataPluginStore
import edu.wpi.inndie.ui.controller.PluginStore
import edu.wpi.inndie.ui.controller.ProcessTestOutputPluginStore
import javafx.geometry.Orientation
import javafx.scene.control.ButtonBar
import software.amazon.awssdk.services.ec2.model.InstanceType
import tornadofx.FX
import tornadofx.Scope
import tornadofx.View
import tornadofx.action
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
import tornadofx.required
import tornadofx.textfield
import tornadofx.toObservable
import tornadofx.validator
import tornadofx.vbox

class PreferencesView : View("Preferences") {

    private val model by inject<PreferencesModel>()
    private val datasetPluginStore by inject<_root_ide_package_.edu.wpi.inndie.ui.controller.DatasetPluginStore>()
    private val loadTestDataPluginManager by inject<_root_ide_package_.edu.wpi.inndie.ui.controller.LoadTestDataPluginStore>()
    private val processTestOutputPluginManager by inject<_root_ide_package_.edu.wpi.inndie.ui.controller.ProcessTestOutputPluginStore>()

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
                    FX.getComponents(scope)[_root_ide_package_.edu.wpi.inndie.ui.controller.PluginStore::class] = datasetPluginStore
                    add(find<PluginManagerEditor>(scope))
                }
                field("Load Test Data") {
                    val scope = Scope()
                    FX.getComponents(scope)[_root_ide_package_.edu.wpi.inndie.ui.controller.PluginStore::class] = loadTestDataPluginManager
                    add(find<PluginManagerEditor>(scope))
                }
                field("Process Test Output") {
                    val scope = Scope()
                    FX.getComponents(scope)[_root_ide_package_.edu.wpi.inndie.ui.controller.PluginStore::class] = processTestOutputPluginManager
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
