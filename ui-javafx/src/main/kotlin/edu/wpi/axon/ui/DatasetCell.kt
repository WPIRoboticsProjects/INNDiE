package edu.wpi.axon.ui

import edu.wpi.axon.tfdata.Dataset
import javafx.scene.control.ListCell

class DatasetCell : ListCell<Dataset>() {

    override fun updateItem(item: Dataset?, empty: Boolean) {
        super.updateItem(item, empty)
        text = if (empty || item == null) {
            null
        } else {
            item.displayName
        }
    }
}
