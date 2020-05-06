package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.Code
import edu.wpi.inndie.dsl.imports.Import
import edu.wpi.inndie.dsl.imports.makeImport
import edu.wpi.inndie.dsl.validator.path.PathValidator
import edu.wpi.inndie.dsl.variable.Variable
import edu.wpi.inndie.util.singleAssign
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
        |subprocess.run(["edgetpu_compiler", "$outputDir/$inputModelFilename", "-o $outputDir"])
        |with open("$outputDir/${getEdgeTpuCompilerLogFilename(inputModelFilename)}", "r") as f:
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
