package edu.wpi.axon.core.dsl.task

import edu.wpi.axon.core.dsl.Configurable
import edu.wpi.axon.core.dsl.Import
import edu.wpi.axon.core.dsl.variable.Code

/**
 * A [Task] is analogous to a method call. If this [Task] has an(y) output variable(s), it should
 * assign to them during [Code.code].
 */
interface Task : Configurable, Code {

    /**
     * The Imports this Tasks' code needs to compile/run.
     */
    val imports: Set<Import>

    /**
     * The [Code] this [Task] depends on. The code generation for these will happen before the
     * code generation for this [Task].
     *
     * TODO: Test this for dependencies of both variables and other tasks
     */
    val dependencies: Set<Code>
}
