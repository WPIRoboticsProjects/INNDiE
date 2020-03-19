package edu.wpi.axon.ui

import edu.wpi.axon.tfdata.optimizer.Optimizer
import javafx.scene.control.ListCell
import kotlin.reflect.KClass

class OptimizerCell : ListCell<KClass<out Optimizer>>() {

    override fun updateItem(item: KClass<out Optimizer>?, empty: Boolean) {
        super.updateItem(item, empty)
        text = if (empty || item == null) {
            null
        } else {
            item.simpleName
        }
    }
}
