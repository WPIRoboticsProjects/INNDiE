package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.util.singleAssign

/**
 * Saves a model to disk.
 */
class SaveModelTask(name: String) : BaseTask(name) {

    /**
     * The model to save.
     */
    var modelInput: Variable by singleAssign()

    /**
     * The filename to save the model as.
     */
    var modelFileName: String by singleAssign()

    override val imports: Set<Import> = setOf(
        makeImport("import tensorflow as tf")
    )

    override val inputs: Set<Variable>
        get() = setOf(modelInput)

    override val outputs: Set<Variable> = setOf()

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    override fun code() = """
        |${modelInput.name}.save("$modelFileName")
    """.trimMargin()
}
