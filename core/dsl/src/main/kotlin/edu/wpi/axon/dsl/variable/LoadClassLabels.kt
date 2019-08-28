package edu.wpi.axon.dsl.variable

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.Import
import edu.wpi.axon.dsl.task.Task
import edu.wpi.axon.dsl.validator.path.PathValidator
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Loads the labels for object classes.
 *
 * TODO: Need to detect the format of the labels or maybe ask the user what it is in the UI
 */
class LoadClassLabels(name: String) : Task(name), KoinComponent {

    /**
     * The path for the class labels file.
     */
    var classLabelsPath: String? = null

    /**
     * The [Variable] to output to.
     */
    var classOutput: Variable? = null

    /**
     * Validates the [classLabelsPath].
     */
    private val pathValidator: PathValidator by inject()

    override val imports: Set<Import>
        get() = setOf()

    override val inputs: Set<Variable>
        get() = setOf()

    override val outputs: Set<Variable>
        get() = setOf(classOutput!!)

    override val dependencies: Set<Code<*>>
        get() = setOf()

    override fun isConfiguredCorrectly() = classLabelsPath != null &&
        pathValidator.isValidPathName(classLabelsPath!!) && classOutput != null &&
        super.isConfiguredCorrectly()

    override fun code() = """
        |${classOutput!!.name} = [line.rstrip('\n') for line in open('${classLabelsPath!!}')]
    """.trimMargin()
}
