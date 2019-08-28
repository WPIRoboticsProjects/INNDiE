package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Configurable
import edu.wpi.axon.dsl.Code

/**
 * A [Task] is analogous to a method call. If this [Task] has an(y) output variable(s), it should
 * assign to them during [Code.code].
 *
 * @param name The name of this task. This does now have an impact on the generated code, it is only
 * used to assure task uniqueness.
 */
abstract class Task(val name: String) : Configurable,
    Code<Code<*>> {

    override fun toString() = "Task(name='$name', class='${this::class.simpleName}')"
}
