package edu.wpi.inndie.tfdata.code.loss

import edu.wpi.axon.tfdata.loss.Loss

interface LossToCode {

    /**
     * Get the code to make a new instance of a [loss].
     *
     * @param loss The [Loss].
     * @return The code to make a new instance of the [loss].
     */
    fun makeNewLoss(loss: Loss): String
}
