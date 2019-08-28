package edu.wpi.axon.core.dsl.task

import edu.wpi.axon.core.dsl.Configurable
import edu.wpi.axon.core.dsl.variable.Code

/**
 * A [Task] is analogous to a method call. If this [Task] has an(y) output variable(s), it should
 * assign to them during [Code.code].
 */
interface Task : Configurable, Code {

    /**
     * The name of this task. This does now have an impact on the generated code, it is only used
     * to assure task uniqueness.
     */
    val name: String
}
