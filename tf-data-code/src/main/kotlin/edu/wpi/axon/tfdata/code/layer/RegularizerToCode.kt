package edu.wpi.axon.tfdata.code.layer

import arrow.core.Either
import edu.wpi.axon.tfdata.layer.Regularizer

interface RegularizerToCode {

    /**
     * Get the code to make a new instance of an [regularizer].
     *
     * @param regularizer The [Regularizer].
     * @return The code to make a new instance of the [regularizer].
     */
    fun makeNewRegularizer(regularizer: Regularizer?): Either<String, String>
}
