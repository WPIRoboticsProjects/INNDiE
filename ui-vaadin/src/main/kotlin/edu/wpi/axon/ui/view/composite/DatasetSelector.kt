package edu.wpi.axon.ui.view.composite

import com.github.mvysny.karibudsl.v10.bind
import com.github.mvysny.karibudsl.v10.comboBox
import com.github.mvysny.karibudsl.v10.formItem
import com.github.mvysny.karibudsl.v10.formLayout
import com.github.mvysny.karibudsl.v10.verticalLayout
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.ui.model.Job

class DatasetSelector : JobComposite() {
    private val root = ui {
        verticalLayout {
            formLayout {
                formItem {
                    comboBox<Dataset>("Dataset") {
                        setItems(Dataset::class.sealedSubclasses.mapNotNull { it.objectInstance })
                        setItemLabelGenerator { it.name }
                        bind(binder).bind(Job::dataset)
                    }
                }
            }
        }
    }
}
