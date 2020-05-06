package edu.wpi.inndie.tfdata.code.optimizer

import edu.wpi.inndie.tfdata.code.pythonString
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

        is Optimizer.RMSprop -> "tf.keras.optimizers.RMSprop(${optimizer.learningRate}, " +
            "${optimizer.rho}, ${optimizer.momentum}, ${optimizer.epsilon}, " +
            "${pythonString(optimizer.centered)})"
    }
}
