package edu.wpi.axon.ui.view.preferences

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.ui.controller.PluginStore
import edu.wpi.axon.ui.model.PluginModel
import javafx.scene.layout.Priority
import tornadofx.Fragment
import tornadofx.bindSelected
import tornadofx.button
import tornadofx.enableWhen
import tornadofx.hbox
import tornadofx.listview
import tornadofx.onUserSelect
import tornadofx.vbox
import tornadofx.vgrow

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
            button(graphic = FontAwesomeIconView(
                FontAwesomeIcon.MINUS
            )
            ) {
                enableWhen { selected.empty.not().and(selected.unofficial) }
                setOnAction {
                    store.removePlugin(selected.item as Plugin.Unofficial)
                }
            }
            button(graphic = FontAwesomeIconView(
                FontAwesomeIcon.PLUS
            )
            ) {
                setOnAction {
                    selected.item = null
                    find<PluginEditor>().openWindow()
                }
            }
        }
    }
}
