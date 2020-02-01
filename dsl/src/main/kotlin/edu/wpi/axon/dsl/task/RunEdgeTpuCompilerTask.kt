package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.validator.path.PathValidator
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.util.singleAssign
import org.koin.core.inject

/**
 * Runs the Coral EdgeTPU compiler to compile a TFLite model for the Coral.
 */
class RunEdgeTpuCompilerTask(name: String) : BaseTask(name) {

    /**
     * The filename of the TFLite model to compile.
     */
    var inputModelFilename: String by singleAssign()

    /**
     * The directory for the compiler to output extra files to.
     */
    var outputDir: String by singleAssign()

    private val pathValidator: PathValidator by inject()

    override val imports: Set<Import> = setOf(
        makeImport("import subprocess")
    )

    override val inputs: Set<Variable> = setOf()

    override val outputs: Set<Variable> = setOf()

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    override fun isConfiguredCorrectly() =
        pathValidator.isValidPathName(inputModelFilename) &&
            inputModelFilename.endsWith(".tflite") && // We get a TFLite as input
            super.isConfiguredCorrectly()

    override fun code(): String = """
        |subprocess.run(["edgetpu_compiler", "$inputModelFilename", "-o $outputDir"])
        |with open("${getEdgeTpuCompilerLogFilename(inputModelFilename)}", "r") as f:
        |    print(f.read())
    """.trimMargin()

    companion object {

        fun getEdgeTpuCompiledModelFilename(inputModelFilename: String) =
            "${getModelBaseName(inputModelFilename)}_edgetpu.tflite"

        fun getEdgeTpuCompilerLogFilename(inputModelFilename: String) =
            "${getModelBaseName(inputModelFilename)}_edgetpu.log"

        private fun getModelBaseName(inputModelFilename: String) =
            inputModelFilename.substringAfterLast('/').substringBeforeLast('.')
    }
}
