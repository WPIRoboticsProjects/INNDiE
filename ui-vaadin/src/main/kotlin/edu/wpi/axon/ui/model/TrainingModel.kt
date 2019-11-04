package edu.wpi.axon.ui.model

import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import kotlin.reflect.KClass

open class TrainingModel {
        var userModelPath: String? = ""

        @NotNull
        var userDataset: KClass<out Dataset>? = null

        @NotNull
        var userOptimizer: KClass<out Optimizer>? = null

        @NotNull
        var userLoss: KClass<out Loss>? = null

        var userMetrics: Set<String> = setOf()

        @Min(5)
        var userEpochs: Int = 0

        var generateDebugComments: Boolean = false

        override fun toString(): String {
                return "TrainingModel(userModelPath=$userModelPath, userDataset=$userDataset, userOptimizer=$userOptimizer, userLoss=$userLoss, userMetrics=$userMetrics, userEpochs=$userEpochs, generateDebugComments=$generateDebugComments)"
        }
}
