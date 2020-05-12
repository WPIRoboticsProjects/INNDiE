package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.Code
import edu.wpi.inndie.dsl.imports.Import
import edu.wpi.inndie.dsl.imports.makeImport
import edu.wpi.inndie.dsl.variable.Variable
import edu.wpi.inndie.tfdata.code.pythonString
import edu.wpi.inndie.util.singleAssign

class CSVLoggerCallbackTask(name: String) : BaseTask(name) {

    /**
     * The filename of the CSV file to log to.
     */
    var logFilePath: String? = null

    /**
     * The character used to separate elements in the CSV file.
     */
    var separator: Char = ','

    /**
     * Whether to overwrite to append to the log file.
     */
    var append: Boolean = false

    /**
     * Where to save the callback to.
     */
    var output: Variable by singleAssign()

    override val imports: Set<Import> = setOf(makeImport("import tensorflow as tf"))

    override val inputs: Set<Variable> = setOf()

    override val outputs: Set<Variable>
        get() = setOf(output)

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    override fun isConfiguredCorrectly() =
        super.isConfiguredCorrectly() && !logFilePath.isNullOrBlank()

    override fun code() = """
        |${output.name} = tf.keras.callbacks.CSVLogger(
        |    ${pythonString(logFilePath)},
        |    separator=${pythonString(separator)},
        |    append=${pythonString(append)}
        |)
    """.trimMargin()
}
