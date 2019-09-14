package edu.wpi.axon.tfdata.optimizer

sealed class Optimizer {

    data class Adam(
        val learningRate: Double,
        val beta1: Double,
        val beta2: Double,
        val epsilon: Double,
        val amsGrad: Boolean
    ) : Optimizer()
}
