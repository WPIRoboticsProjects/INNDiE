package edu.wpi.inndie.tfdata.code.layer

import arrow.core.Either
import edu.wpi.inndie.tfdata.layer.Constraint

interface ConstraintToCode {

    /**
     * Get the code to make a new instance of an [constraint].
     *
     * @param constraint The [Constraint].
     * @return The code to make a new instance of the [constraint].
     */
    fun makeNewConstraint(constraint: Constraint): Either<String, String>
}
