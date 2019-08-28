package edu.wpi.axon.core.dsl.variable

import edu.wpi.axon.core.dsl.Import

/**
 * A "typeclass" for generating code. The code this interface means is (possibly multiline)
 * computations that can assign to variables and can depends on other variables or computations.
 */
interface Code<out T : Code<T>> {

    /**
     * The Imports this [Code] needs to compile/run.
     */
    val imports: Set<Import>

    /**
     * The variable inputs to this [Code].
     */
    val inputs: Set<Variable>

    /**
     * The variable outputs from this [Code].
     */
    val outputs: Set<Variable>

    /**
     * The [Code] this [Code] depends on. The code generation for these will happen before the
     * code generation for this [Code].
     *
     * TODO: Test this for dependencies of both variables and other tasks
     */
    val dependencies: Set<T>

    /**
     * @return The code for this component.
     */
    fun code(): String
}
