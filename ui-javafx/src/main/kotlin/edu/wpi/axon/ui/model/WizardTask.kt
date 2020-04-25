package edu.wpi.axon.ui.model

import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer

data class TaskInput(val title: String = "", val description: String = "", val graphic: String = "", val datasetType: DatasetType, val dataset: Dataset, val optimizer: Optimizer, val loss: Loss)

data class WizardTask(val title: String = "", val description: String = "", val graphic: String = "", val supportedInputs: List<TaskInput> = listOf())
