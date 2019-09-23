package edu.wpi.axon.ui.view

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.bind
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.comboBox
import com.github.mvysny.karibudsl.v10.formItem
import com.github.mvysny.karibudsl.v10.formLayout
import com.github.mvysny.karibudsl.v10.label
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.data.binder.BeanValidationBinder
import com.vaadin.flow.router.Route
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.ui.AxonLayout
import edu.wpi.axon.ui.model.Training
import kotlin.reflect.KClass

@Route(layout = AxonLayout::class)
class TrainingView : KComposite() {
    private val binder = BeanValidationBinder<Training>(Training::class.java)

    private val root = ui {
        verticalLayout {
            val infoLabel = label {
                text = "Status"
            }
            formLayout {
                formItem {
                    comboBox<KClass<out Dataset>>("Dataset") {
                        setItems(Dataset::class.sealedSubclasses)
                        setItemLabelGenerator {
                            it.simpleName
                        }
                        isRequired = true
                        bind(binder)
                    }
                }
                formItem {
                    comboBox<KClass<out Optimizer>>("Optimizer") {
                        setItems(Optimizer::class.sealedSubclasses)
                        setItemLabelGenerator {
                            it.simpleName
                        }
                        isRequired = true
                        bind(binder)
                    }
                }
                formItem {
                    comboBox<KClass<out Loss>>("Loss") {
                        setItems(Loss::class.sealedSubclasses)
                        setItemLabelGenerator {
                            it.simpleName
                        }
                        isRequired = true
                        bind(binder)
                    }
                }
                formItem {
                    comboBox<KClass<out Loss>>("Loss") {
                        setItems(Loss::class.sealedSubclasses)
                        setItemLabelGenerator {
                            it.simpleName
                        }
                        isRequired = true
                        bind(binder)
                    }
                }
                button("Generate") {
                    onLeftClick {
                        val training = Training()
                        if (binder.writeBeanIfValid(training)) {
                            infoLabel.text = "Saved bean values: $training"
                        } else {
                            val validate = binder.validate()
                            infoLabel.text = "There are errors: $validate"
                        }
                    }
                }
            }
        }
    }
}
