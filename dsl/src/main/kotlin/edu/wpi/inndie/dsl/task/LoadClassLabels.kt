package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.Code
import edu.wpi.inndie.dsl.imports.Import
import edu.wpi.inndie.dsl.validator.path.PathValidator
import edu.wpi.inndie.dsl.variable.Variable
import edu.wpi.inndie.util.singleAssign
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Loads the labels for object classes.
 *
 * TODO: Need to detect the format of the labels or maybe ask the user what it is in the UI
 */
class LoadClassLabels(name: String) : BaseTask(name), KoinComponent {

    /**
     * The path for the class labels file.
     */
    var classLabelsPath: String by singleAssign()

    /**
     * The [Variable] to output to.
     */
    var classOutput: Variable by singleAssign()

    /**
     * Validates the [classLabelsPath].
     */
    private val pathValidator: PathValidator by inject()

    override val imports: Set<Import> = setOf()

    override val inputs: Set<Variable> = setOf()

    override val outputs: Set<Variable>
        get() = setOf(classOutput)

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    override fun isConfiguredCorrectly() = pathValidator.isValidPathName(classLabelsPath) &&
        super.isConfiguredCorrectly()

    override fun code() = """
        |${classOutput.name} = [line.rstrip('\n') for line in open('$classLabelsPath')]
    """.trimMargin()
}
