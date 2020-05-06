package edu.wpi.inndie.ui.model

import edu.wpi.axon.plugin.Plugin
import edu.wpi.inndie.ui.controller.PluginStore
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.ItemViewModel

class PluginModel : ItemViewModel<Plugin>() {

    val store by inject<_root_ide_package_.edu.wpi.inndie.ui.controller.PluginStore>()

    val name = bind { SimpleStringProperty(item?.name ?: "") }
    val contents = bind { SimpleStringProperty(item?.contents ?: "") }

    val unofficial = bind { SimpleBooleanProperty(item is Plugin.Unofficial) } as ReadOnlyBooleanProperty

    override fun onCommit() {
        if (item is Plugin.Unofficial) {
            store.removePlugin(item as Plugin.Unofficial)
        }

        item = Plugin.Unofficial(name.value, contents.value)
        store.addPlugin(item as Plugin.Unofficial)
    }
}
