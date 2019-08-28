package edu.wpi.axon.dsl.variable

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.Import
import edu.wpi.axon.dsl.task.Task
import edu.wpi.axon.dsl.validator.path.PathValidator
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Loads an image.
 *
 * TODO: Validate image format
 */
class LoadImageData(name: String) : Task(name), KoinComponent {

    /**
     * The file path to load this data from.
     */
    var path: String? = null

    /**
     * The output the image data will be stored in.
     */
    var imageDataOutput: Variable? = null

    /**
     * The output the image size will be stored in.
     */
    var imageSizeOutput: Variable? = null

    /**
     * Validates the [path].
     */
    private val pathValidator: PathValidator by inject()

    override val imports = setOf(
        Import.ModuleAndIdentifier("PIL", "Image"),
        Import.ModuleAndName("numpy", "np")
    )

    override val inputs: Set<Variable> = emptySet()

    override val outputs: Set<Variable>
        get() = setOf(imageDataOutput!!, imageSizeOutput!!)

    override val dependencies: Set<Code<*>> = emptySet()

    override fun isConfiguredCorrectly() = path != null && pathValidator.isValidPathName(path!!) &&
        imageDataOutput != null && imageSizeOutput != null

    override fun code() = """
        |$name = Image.open('${path!!}')
        |${imageDataOutput!!.name} = preprocess($name)
        |${imageSizeOutput!!.name} = np.array([$name.size[1], $name.size[0]], dtype=np.float32).reshape(1, 2)
    """.trimMargin()
}
