package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.validator.path.PathValidator
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.util.singleAssign
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Loads an image from a file.
 */
class LoadImageTask(name: String) : BaseTask(name), KoinComponent {

    /**
     * The file path to load this data from.
     */
    var imagePath: String by singleAssign()

    /**
     * The output the image will be stored in.
     */
    var imageOutput: Variable by singleAssign()

    /**
     * Validates the [imagePath].
     */
    private val pathValidator: PathValidator by inject()

    override val imports = setOf(makeImport("from PIL import Image"))

    override val inputs: Set<Variable> = emptySet()

    override val outputs: Set<Variable>
        get() = setOf(imageOutput)

    override val dependencies: Set<Code<*>> = emptySet()

    override fun isConfiguredCorrectly() = pathValidator.isValidPathName(imagePath) &&
        super.isConfiguredCorrectly()

    override fun code() = """
        |${imageOutput.name} = Image.open('$imagePath')
    """.trimMargin()
}
