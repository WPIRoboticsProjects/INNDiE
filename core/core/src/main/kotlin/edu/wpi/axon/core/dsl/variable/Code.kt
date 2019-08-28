package edu.wpi.axon.core.dsl.variable

/**
 * A "typeclass" for generating code. The code this interface means is (possibly multiline)
 * computations that can assign to variables and can depends on other variables or computations.
 */
interface Code {

    /**
     * @return The code for this component.
     */
    fun code(): String
}
