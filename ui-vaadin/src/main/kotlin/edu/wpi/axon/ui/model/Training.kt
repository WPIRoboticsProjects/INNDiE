package edu.wpi.axon.ui.model

import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.layer.SealedLayer
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer

data class Training (
    var userModelPath: String? = "",
    var userDataset: Dataset? = null,
    var userOptimizer: Optimizer? = null,
    var userLoss: Loss? = null,
    var userMetrics: Set<String> = setOf(),
    var userEpochs: Int = 0,
    var userNewLayers: Set<SealedLayer.MetaLayer> = setOf(),
    var generateDebugComments: Boolean = false
)
