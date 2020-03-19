package edu.wpi.axon.ui

import edu.wpi.axon.plugin.Plugin
import javafx.scene.control.ListCell

class PluginCell : ListCell<Plugin>() {

    override fun updateItem(item: Plugin?, empty: Boolean) {
        super.updateItem(item, empty)
        text = if (empty || item == null) {
            null
        } else {
            item.name
        }
    }
}
