package edu.wpi.axon.ui.model

import edu.wpi.axon.aws.preferences.Preferences
import edu.wpi.axon.aws.preferences.PreferencesManager
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleObjectProperty
import tornadofx.ItemViewModel

class AwsPreferencesModel: ItemViewModel<Preferences>() {
    private val preferencesManager by di<PreferencesManager>()

    init {
        itemProperty.set(preferencesManager.get())
    }

    val defaultEC2NodeTypeProperty = bind { SimpleObjectProperty(item.defaultEC2NodeType) }
    val statusPollingDelayProperty = bind { SimpleLongProperty(item.statusPollingDelay) }

    override fun onCommit() {
        item = Preferences(defaultEC2NodeTypeProperty.value, statusPollingDelayProperty.value.toLong())
        preferencesManager.put(item)
    }
}
