package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.inndie.util.singleAssign

/**
 * Saves a model to disk.
 */
class SaveModelTask(name: String) : BaseTask(name) {

    /**
     * The model to save.
     */
    var modelInput: Variable by singleAssign()

    /**
     * The path to save the model to, ending in the filename of the model. Any parent directories
     * that do not already exist will be created.
     */
    var modelPath: String by singleAssign()

    override val imports: Set<Import> = setOf(
        makeImport("import tensorflow as tf"),
        makeImport("import os"),
        makeImport("import errno"),
        makeImport("from pathlib import Path")
    )

    override val inputs: Set<Variable>
        get() = setOf(modelInput)

    override val outputs: Set<Variable> = setOf()

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    override fun code() = """
        |try:
        |    os.makedirs(Path("$modelPath").parent)
        |except OSError as err:
        |    if err.errno != errno.EEXIST:
        |        raise
        |
        |${modelInput.name}.save("$modelPath")
    """.trimMargin()
}
