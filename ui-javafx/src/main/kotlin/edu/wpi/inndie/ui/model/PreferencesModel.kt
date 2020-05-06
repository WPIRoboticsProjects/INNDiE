package edu.wpi.inndie.ui.model

import edu.wpi.inndie.aws.preferences.Preferences
import edu.wpi.inndie.aws.preferences.PreferencesManager
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleObjectProperty
import tornadofx.ItemViewModel

class PreferencesModel : ItemViewModel<Preferences>() {

    private val preferencesManager by di<PreferencesManager>()

    init {
        itemProperty.set(preferencesManager.get())
    }

    val defaultEC2NodeTypeProperty = bind { SimpleObjectProperty(item.defaultEC2NodeType) }
    val statusPollingDelayProperty = bind { SimpleLongProperty(item.statusPollingDelay) }

    override fun onCommit() {
        item = Preferences(
            defaultEC2NodeType = defaultEC2NodeTypeProperty.value,
            statusPollingDelay = statusPollingDelayProperty.value.toLong()
        )
        preferencesManager.put(item)
    }
}
