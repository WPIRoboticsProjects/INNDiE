package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.Code
import edu.wpi.inndie.dsl.imports.Import
import edu.wpi.inndie.dsl.variable.Variable
import edu.wpi.inndie.util.singleAssign

/**
 * Runs a plugin by emitting its definition, calling it with some input variables, and saving its
 * output to some output variables.
 */
class RunPluginTask(name: String) : BaseTask(name) {

    /**
     * The name of the function to call.
     */
    var functionName: String by singleAssign()

    /**
     * The full function definition.
     */
    var functionDefinition: String by singleAssign()

    /**
     * The variables to pass to the function.
     */
    var functionInputs: List<Variable> by singleAssign()

    /**
     * The variables to save from the function return value.
     */
    var functionOutputs: List<Variable> by singleAssign()

    override val imports: Set<Import> = setOf()

    override val inputs: Set<Variable>
        get() = functionInputs.toSet()

    override val outputs: Set<Variable>
        get() = functionOutputs.toSet()

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    override fun isConfiguredCorrectly() =
        inputs.all { it.isConfiguredCorrectly() } &&
            outputs.all { it.isConfiguredCorrectly() } &&
            super.isConfiguredCorrectly()

    override fun code(): String {
        val functionCall = """$functionName(${functionInputs.joinToString { it.name }})"""

        val outputString = if (functionOutputs.isEmpty()) {
            ""
        } else {
            """(${functionOutputs.joinToString { it.name }}) = """
        }

        // Two empty lines before and after the definition to ensure the definition is isolated from
        // the rest of the script
        return """
            |
            |
            |$functionDefinition
            |
            |
            |$outputString$functionCall
        """.trimMargin()
    }
}
