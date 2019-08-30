package edu.wpi.axon.dsl.task

/**
 * Holds the common implementation details for Tasks.
 */
abstract class BaseTask(override val name: String) : Task {

    // TODO: Add import validation
    override fun isConfiguredCorrectly() =
        inputs.all { it.isConfiguredCorrectly() } && outputs.all { it.isConfiguredCorrectly() }

    override fun toString() = "Task(name='$name', class='${this::class.simpleName}')"
}
