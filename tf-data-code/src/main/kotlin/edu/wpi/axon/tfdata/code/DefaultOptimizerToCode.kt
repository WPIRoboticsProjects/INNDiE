package edu.wpi.axon.tfdata.code

import edu.wpi.axon.tfdata.optimizer.Optimizer

class DefaultOptimizerToCode : OptimizerToCode {

    override fun makeNewOptimizer(optimizer: Optimizer) = when (optimizer) {
        is Optimizer.Adam -> "tf.keras.optimizers.Adam(${optimizer.learningRate}, " +
            "${optimizer.beta1}, ${optimizer.beta2}, ${optimizer.epsilon}, " +
            "${boolToPythonString(optimizer.amsGrad)})"
    }
}
