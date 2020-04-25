package edu.wpi.axon.ui.model

import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.training.ModelDeploymentTarget
import kotlin.reflect.KClass

data class TaskInput(val title: String = "", val description: String = "", val graphic: String? = null, val datasetType: DatasetType, val dataset: Dataset, val optimizer: Optimizer, val loss: Loss)

data class WizardTask(val title: String = "", val description: String = "", val graphic: String? = null, val supportedInputs: List<TaskInput> = listOf())

data class WizardTarget(val title: String = "", val description: String = "", val graphic: String? = null, val targetClass: KClass<out ModelDeploymentTarget>)
