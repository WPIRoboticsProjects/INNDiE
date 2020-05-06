package edu.wpi.axon.ui.view.jobeditor

import edu.wpi.axon.plugin.PluginManager
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.training.ModelDeploymentTarget
import edu.wpi.axon.ui.model.JobModel
import edu.wpi.axon.ui.view.isIntGreaterThanOrEqualTo
import edu.wpi.inndie.util.datasetPluginManagerName
import javafx.stage.Modality
import kotlin.reflect.KClass
import tornadofx.Fragment
import tornadofx.action
import tornadofx.button
import tornadofx.combobox
import tornadofx.field
import tornadofx.fieldset
import tornadofx.filterInput
import tornadofx.form
import tornadofx.hbox
import tornadofx.isInt
import tornadofx.label
import tornadofx.separator
import tornadofx.textfield
import tornadofx.toObservable
import tornadofx.tooltip
import tornadofx.validator
import tornadofx.vbox

class JobConfiguration : Fragment("Configuration") {
    private val job by inject<JobModel>()
    private val datasetPluginManager by di<PluginManager>(datasetPluginManagerName)

    override val root = form {
        hbox(20) {
            vbox(20) {
                fieldset("Dataset") {
                    label("This is the data the model will be trained with.")
                    add<DatasetPicker>()
                }
                separator()
                fieldset("Model") {
                    label("This is the model that will be trained.")
                    add<ModelPicker>()
                }
                separator()
                fieldset("Dataset Plugin") {
                    label("This adapts the shape of the dataset to the shape the model requires.")
                    field("Plugin") {
                        tooltip(
                            """
                            The plugin used to process the dataset before giving it to the model for training.
                            Add new plugins in the plugin editor.
                            """.trimIndent()
                        )
                        combobox(job.datasetPlugin) {
                            items = datasetPluginManager.listPlugins().toList().toObservable()
                            cellFormat {
                                text = it.name.toLowerCase().capitalize()
                            }
                        }
                    }
                }
            }
            vbox(20) {
                fieldset("General") {
                    field("Epochs") {
                        tooltip(
                            """
                            The number of iterations over the dataset preformed when training the model.
                            More epochs takes longer but usually produces a more accurate model.
                            """.trimIndent()
                        )
                        textfield(job.userEpochs) {
                            filterInput { it.controlNewText.isInt() }
                            validator { it.isIntGreaterThanOrEqualTo(1) }
                        }
                    }
                }
                separator()
                fieldset("Optimizer") {
                    field("Type") {
                        combobox(job.optimizerType) {
                            tooltip(
                                """
                                The type of the optimizer to use when training the model.
                                Different optimizers are better for different models.
                                """.trimIndent()
                            )
                            items = Optimizer::class.sealedSubclasses.toObservable()
                            cellFormat {
                                text = it.simpleName ?: "UNKNOWN"
                            }
                            valueProperty().addListener { _, _, newValue ->
                                if (newValue != null) {
                                    // Make an empty optimizer of the new type for the
                                    // OptimizerFragment to edit
                                    job.userOptimizer.value = newValue.objectInstanceOrEmptyCtor()
                                }
                            }
                        }
                    }
                    field {
                        button("Edit") {
                            action {
                                find<OptimizerFragment>().openModal(modality = Modality.WINDOW_MODAL)
                            }
                        }
                    }
                }
                separator()
                fieldset("Loss") {
                    field("Type") {
                        combobox(job.lossType) {
                            tooltip(
                                """
                                The type of the loss function to use.
                                Different loss functions are better for different models and tasks.
                                """.trimIndent()
                            )
                            items = Loss::class.sealedSubclasses.toObservable()
                            cellFormat {
                                text = it.simpleName ?: "UNKNOWN"
                            }
                            valueProperty().addListener { _, _, newValue ->
                                if (newValue != null) {
                                    // Make an empty optimizer of the new type for the
                                    // LossFragment to edit
                                    job.userLoss.value = newValue.objectInstanceOrEmptyCtor()
                                }
                            }
                        }
                    }
                    field {
                        button("Edit") {
                            action {
                                find<LossFragment>().openModal(modality = Modality.WINDOW_MODAL)
                            }
                        }
                    }
                }
                fieldset("Target") {
                    field("Type") {
                        combobox(job.targetType) {
                            tooltip(
                                """
                                The target machine that the model will run on.
                                """.trimIndent()
                            )
                            items = ModelDeploymentTarget::class.sealedSubclasses.toObservable()
                            cellFormat {
                                text = it.simpleName ?: "UNKNOWN"
                            }
                            valueProperty().addListener { _, _, newValue ->
                                if (newValue != null) {
                                    job.target.value = newValue.objectInstanceOrEmptyCtor()
                                }
                            }
                        }
                    }
                    field {
                        button("Edit") {
                            action {
                                find<TargetFragment>().openModal(modality = Modality.WINDOW_MODAL)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun <T : Any> KClass<T>.objectInstanceOrEmptyCtor(): T =
    objectInstance ?: constructors.first { it.parameters.isEmpty() }.call()
