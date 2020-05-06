package edu.wpi.inndie.tfdata.code.optimizer

import edu.wpi.axon.tfdata.optimizer.Optimizer

interface OptimizerToCode {

    /**
     * Get the code to make a new instance of an [optimizer].
     *
     * @param optimizer The [Optimizer].
     * @return The code to make a new instance of the [optimizer].
     */
    fun makeNewOptimizer(optimizer: Optimizer): String
}
