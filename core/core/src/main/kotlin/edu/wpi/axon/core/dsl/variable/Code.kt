package edu.wpi.axon.core.dsl.variable

import edu.wpi.axon.core.dsl.Import
import edu.wpi.axon.core.dsl.task.Task

/**
 * A "typeclass" for generating code. The code this interface means is (possibly multiline)
 * computations that can assign to variables and can depends on other variables or computations.
 */
interface Code {

    /**
     * The Imports this [Code] needs to compile/run.
     */
    val imports: Set<Import>

    /**
     * The [Code] this [Task] depends on. The code generation for these will happen before the
     * code generation for this [Code].
     *
     * TODO: Test this for dependencies of both variables and other tasks
     */
    val dependencies: Set<Code>

    /**
     * @return The code for this component.
     */
    fun code(): String
}
