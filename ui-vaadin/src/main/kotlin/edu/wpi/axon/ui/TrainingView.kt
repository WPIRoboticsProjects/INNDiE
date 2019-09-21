package edu.wpi.axon.ui

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.router.Route
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import kotlin.reflect.KClass

@Route("training")
class TrainingView: KComposite() {
    private val root = ui {
        verticalLayout {
            comboBox<KClass <out Dataset>>("Dataset") {
                setItems(Dataset::class.sealedSubclasses)
                setItemLabelGenerator {
                    it.simpleName
                }
            }
            comboBox<KClass <out Optimizer>>("Optimizer") {
                setItems(Optimizer::class.sealedSubclasses)
                setItemLabelGenerator {
                    it.simpleName
                }
            }
            comboBox<KClass <out Loss>>("Loss") {
                setItems(Loss::class.sealedSubclasses)
                setItemLabelGenerator {
                    it.simpleName
                }
            }
        }
    }
}

@VaadinDsl
fun (@VaadinDsl HasComponents).trainingView(block: (@VaadinDsl TrainingView).()->Unit = {}) = init(TrainingView(), block)
