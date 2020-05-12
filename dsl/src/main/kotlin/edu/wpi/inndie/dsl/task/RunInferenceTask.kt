package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.Code
import edu.wpi.inndie.dsl.imports.Import
import edu.wpi.inndie.dsl.variable.Variable
import edu.wpi.inndie.util.singleAssign

/**
 * Runs inference.
 */
class RunInferenceTask(name: String) : BaseTask(name) {

    /**
     * The model to run inference with.
     */
    var model by singleAssign<Variable>()

    /**
     * The data to give to the model.
     */
    var input by singleAssign<Variable>()

    /**
     * The steps to give to predict.
     */
    var steps by singleAssign<Variable>()

    /**
     * The variable to save the inference output to.
     */
    var output by singleAssign<Variable>()

    override val imports: Set<Import> = setOf()

    override val inputs: Set<Variable>
        get() = setOf(model, input, steps)

    override val outputs: Set<Variable>
        get() = setOf(output)

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    override fun code(): String {
        return """
            |${output.name} = ${model.name}.predict(${input.name}, steps=${steps.name})
        """.trimMargin()
    }
}
