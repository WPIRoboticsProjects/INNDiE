package edu.wpi.axon.ui.view

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.bind
import com.github.mvysny.karibudsl.v10.comboBox
import com.github.mvysny.karibudsl.v10.formItem
import com.github.mvysny.karibudsl.v10.formLayout
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.data.binder.BeanValidationBinder
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.VaadinSession
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.ui.AxonLayout
import edu.wpi.axon.ui.model.TrainingModel
import kotlin.reflect.KClass

@Route(layout = AxonLayout::class)
class DatasetView : KComposite() {
    private val binder = BeanValidationBinder<TrainingModel>(TrainingModel::class.java)

    private val root = ui {
        verticalLayout {
            formLayout {
                formItem {
                    comboBox<KClass<out Dataset>>("Dataset") {
                        setItems(Dataset::class.sealedSubclasses)
                        setItemLabelGenerator {
                            it.simpleName
                        }

                        bind(binder).bind(TrainingModel::userDataset)
                    }
                }
            }
        }
    }

    init {
        binder.bean = VaadinSession.getCurrent().getAttribute(TrainingModel::class.java)
    }
}
