package edu.wpi.axon.ui.model

import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.plugin.PluginManager
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import tornadofx.ItemViewModel
import tornadofx.Scope
import tornadofx.toObservable

class PluginModel: ItemViewModel<Plugin>() {
    val name = bind { SimpleStringProperty(item?.name ?: "") }
    val contents = bind { SimpleStringProperty(item?.contents ?: "") }

    override fun onCommit() {

    }
}

class PluginManagerModel: ItemViewModel<PluginManager>() {
    val plugins = bind { SimpleListProperty<Plugin>((item?.listPlugins() ?: listOf<Plugin>()).toList().toObservable()) }

    override fun onCommit() {

    }
}

class PluginManagerScope(val manager: PluginManagerModel): Scope()
