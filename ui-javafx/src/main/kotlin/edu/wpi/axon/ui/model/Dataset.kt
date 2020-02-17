package edu.wpi.axon.ui.model

import edu.wpi.axon.tfdata.Dataset
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.ItemViewModel

class DatasetModel: ItemViewModel<Dataset>() {
    val type = bind { SimpleObjectProperty<DatasetType>(
            item.let {
                when (it) {
                    is Dataset.ExampleDataset -> DatasetType.EXAMPLE
                    is Dataset.Custom -> DatasetType.CUSTOM
                    else -> null
                }
            }
    ) }

    val name = bind { SimpleStringProperty(item?.displayName ?: "") }
}

enum class DatasetType {
    EXAMPLE, CUSTOM
}
