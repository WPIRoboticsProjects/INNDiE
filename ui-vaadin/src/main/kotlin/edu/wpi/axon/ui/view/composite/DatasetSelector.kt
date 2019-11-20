package edu.wpi.axon.ui.view.composite

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.comboBox
import com.github.mvysny.karibudsl.v10.formItem
import com.github.mvysny.karibudsl.v10.formLayout
import com.github.mvysny.karibudsl.v10.verticalLayout
import edu.wpi.axon.tfdata.Dataset

class DatasetSelector : KComposite() {
    private val root = ui {
        verticalLayout {
            formLayout {
                formItem {
                    comboBox<Dataset>("Dataset") {
                        setItems(Dataset.ExampleDataset::class.sealedSubclasses.mapNotNull { it.objectInstance })
                        setItemLabelGenerator { it.displayName }
                    }
                }
            }
        }
    }
}
