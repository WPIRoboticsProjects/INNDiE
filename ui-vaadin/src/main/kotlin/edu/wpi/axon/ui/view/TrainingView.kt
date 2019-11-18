package edu.wpi.axon.ui.view

import com.github.mvysny.karibudsl.v10.KComposite
import com.vaadin.flow.router.Route
import edu.wpi.axon.ui.AxonLayout

@Route(layout = AxonLayout::class)
class TrainingView : KComposite() {
    // private val binder = BeanValidationBinder<TrainingModel>(TrainingModel::class.java)
    //
    // private val root = ui {
    //     verticalLayout {
    //         val infoLabel = label {
    //             text = "Info"
    //         }
    //         formLayout {
    //             formItem {
    //                 comboBox<KClass<out Optimizer>>("Optimizer") {
    //                     setItems(Optimizer::class.sealedSubclasses)
    //                     setItemLabelGenerator {
    //                         it.simpleName
    //                     }
    //
    //                     bind(binder).bind(TrainingModel::userOptimizer)
    //                 }
    //             }
    //             formItem {
    //                 comboBox<KClass<out Loss>>("Loss") {
    //                     setItems(Loss::class.sealedSubclasses)
    //                     setItemLabelGenerator {
    //                         it.simpleName
    //                     }
    //
    //                     bind(binder).bind(TrainingModel::userLoss)
    //                 }
    //             }
    //             formItem {
    //                 numberField("Epochs") {
    //                     setHasControls(true)
    //                     isPreventInvalidInput = true
    //
    //                     bind(binder)
    //                             .asRequired()
    //                             .withValidator { value, _ ->
    //                                 if (value.isWholeNumber()) {
    //                                     ValidationResult.ok()
    //                                 } else {
    //                                     ValidationResult.error("Must be an integer!")
    //                                 }
    //                             }
    //                             .toInt()
    //                             .bind(TrainingModel::userEpochs)
    //                 }
    //             }
    //             formItem {
    //                 checkBox("Generate Debug Output") {
    //                     bind(binder).bind(TrainingModel::generateDebugComments)
    //                 }
    //             }
    //             button("Generate") {
    //                 onLeftClick {
    //                     val validate = binder.validate()
    //                     if (validate.isOk) {
    //                         infoLabel.text = "Saved bean values: ${binder.bean}"
    //                     } else {
    //                         infoLabel.text = "There are errors: ${validate.validationErrors}"
    //                     }
    //                 }
    //             }
    //         }
    //     }
    // }
    //
    // init {
    //     binder.bean = VaadinSession.getCurrent().getAttribute(TrainingModel::class.java)
    // }
}
