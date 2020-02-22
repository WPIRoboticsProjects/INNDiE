package edu.wpi.axon.tfdata.code.optimizer

import edu.wpi.axon.tfdata.code.pythonString
import edu.wpi.axon.tfdata.optimizer.Optimizer

class DefaultOptimizerToCode : OptimizerToCode {

    override fun makeNewOptimizer(optimizer: Optimizer) = when (optimizer) {
        is Optimizer.Adam -> "tf.keras.optimizers.Adam(${optimizer.learningRate}, " +
            "${optimizer.beta1}, ${optimizer.beta2}, ${optimizer.epsilon}, " +
            "${pythonString(optimizer.amsGrad)})"

        is Optimizer.FTRL -> "tf.keras.optimizers.Ftrl(${optimizer.learningRate}, " +
            "${optimizer.learningRatePower}, ${optimizer.initialAccumulatorValue}, " +
            "${optimizer.l1RegularizationStrength}, ${optimizer.l2RegularizationStrength}, " +
            "'Ftrl', ${optimizer.l2ShrinkageRegularizationStrength})"
    }
}
