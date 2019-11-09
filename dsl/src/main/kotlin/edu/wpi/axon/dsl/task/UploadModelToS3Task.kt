package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.util.singleAssign
import org.koin.core.KoinComponent

/**
 * Uploads a model to S3. This only performs a side-effect, so there are no [inputs] nor [outputs].
 */
class UploadModelToS3Task(name: String) : BaseTask(name), KoinComponent {

    /**
     * The name of the model on disk.
     */
    var modelName: String by singleAssign()

    /**
     * The name of the S3 bucket.
     */
    var bucketName: String by singleAssign()

    /**
     * The region.
     */
    var region: String by singleAssign()

    override val imports = setOf(makeImport("import axonawsclient"))

    override val inputs: Set<Variable> = emptySet()

    override val outputs: Set<Variable> = setOf()

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    override fun code() = """
        |axonawsclient.impl_upload_model_file("$modelName", "$bucketName", "$region")
    """.trimMargin()
}
