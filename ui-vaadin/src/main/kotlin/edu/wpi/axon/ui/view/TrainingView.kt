package edu.wpi.axon.ui.view

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.bind
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.checkBox
import com.github.mvysny.karibudsl.v10.comboBox
import com.github.mvysny.karibudsl.v10.formItem
import com.github.mvysny.karibudsl.v10.formLayout
import com.github.mvysny.karibudsl.v10.label
import com.github.mvysny.karibudsl.v10.numberField
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.data.binder.BeanValidationBinder
import com.vaadin.flow.router.Route
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.ui.AxonLayout
import edu.wpi.axon.ui.model.TrainingModel
import kotlin.reflect.KClass

@Route(layout = AxonLayout::class)
class TrainingView : KComposite() {

    private val binder = BeanValidationBinder<TrainingModel>(TrainingModel::class.java)

    private val root = ui {
        verticalLayout {
            val infoLabel = label {
                text = "Info"
            }
            formLayout {
                formItem {
                    comboBox<KClass<out Dataset>>("Dataset") {
                        setItems(Dataset::class.sealedSubclasses)
                        setItemLabelGenerator {
                            it.simpleName
                        }
                        isRequired = true
                        bind(binder).bind(TrainingModel::userDataset)
                    }
                }
                formItem {
                    comboBox<KClass<out Optimizer>>("Optimizer") {
                        setItems(Optimizer::class.sealedSubclasses)
                        setItemLabelGenerator {
                            it.simpleName
                        }
                        isRequired = true
                    }
                }
                formItem {
                    comboBox<KClass<out Loss>>("Loss") {
                        setItems(Loss::class.sealedSubclasses)
                        setItemLabelGenerator {
                            it.simpleName
                        }
                        isRequired = true
                    }
                }
                formItem {
                    numberField("Epochs") {
                        isPreventInvalidInput = true

                        bind(binder).bind(TrainingModel::userEpochs)
                    }
                }
                formItem {
                    checkBox("Generate Debug Output")
                }
                button("Generate") {
                    onLeftClick {
                        binder.validate()
                        val e = TrainingModel()
                        if (binder.writeBeanIfValid(e)) {
                            infoLabel.text = "Saved bean values: $e"
                        } else {
                            val validate = binder.validate()
                            infoLabel.text = "There are errors: ${validate.validationErrors}";
                        }
                    }
                }
            }
        }
    }
}
