package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.AnyCode
import edu.wpi.inndie.dsl.Code
import edu.wpi.inndie.dsl.Configurable

/**
 * A [Task] is analogous to a method call. If this [Task] has an(y) output variable(s), it should
 * assign to them during [Code.code].
 */
interface Task : Configurable,
    AnyCode {

    /**
     * The name of this task. This does now have an impact on the generated code, it is only used to
     * assure task uniqueness.
     */
    val name: String

    override val dependencies: MutableSet<Code<*>>

    /**
     * A toString that requires the task [isConfiguredCorrectly].
     */
    fun unsafeToString() = """
        |Task(
        |    name=$name,
        |    class=${this::class.simpleName},
        |    inputs=${inputs.joinToString()},
        |    outputs=${outputs.joinToString()},
        |    dependencies=${dependencies.joinToString()}
        |)
    """.trimMargin()
}
