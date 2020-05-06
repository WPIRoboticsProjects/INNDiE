package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.UniqueVariableNameGenerator
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.validator.path.PathValidator
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.inndie.util.singleAssign
import org.koin.core.inject

/**
 * Quantizes a model for the Coral Edge TPU using post-training quantization.
 */
class PostTrainingQuantizationTask(name: String) : BaseTask(name) {

    /**
     * The filename of the model to load for quantization.
     */
    var modelFilename: String by singleAssign()

    /**
     * The filename of the output model to write to disk.
     */
    var outputModelFilename: String by singleAssign()

    /**
     * The Tensor holding the representative dataset which will be wrapped in a generator and given
     * to TF.
     */
    var representativeDataset: Variable by singleAssign()

    private val pathValidator: PathValidator by inject()
    private val uniqueVariableNameGenerator: UniqueVariableNameGenerator by inject()

    override val imports: Set<Import> = setOf(
        makeImport("import tensorflow as tf")
    )

    override val inputs: Set<Variable>
        get() = setOf(representativeDataset)

    override val outputs: Set<Variable> = setOf()

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    override fun isConfiguredCorrectly() =
        pathValidator.isValidPathName(modelFilename) &&
            pathValidator.isValidPathName(outputModelFilename) &&
            modelFilename.endsWith(".h5") && // We get an HDF5 as input
            outputModelFilename.endsWith(".tflite") && // We output a TFLite file
            super.isConfiguredCorrectly()

    override fun code(): String {
        val representativeDatasetGen = uniqueVariableNameGenerator.uniqueVariableName()
        val datasetElement = uniqueVariableNameGenerator.uniqueVariableName()
        val converter = uniqueVariableNameGenerator.uniqueVariableName()
        val tfliteModel = uniqueVariableNameGenerator.uniqueVariableName()
        return """
            |def $representativeDatasetGen():
            |    for $datasetElement in ${representativeDataset.name}:
            |        yield [[$datasetElement]]
            |
            |$converter = tf.lite.TFLiteConverter.from_keras_model_file("$modelFilename")
            |$converter.optimizations = [tf.lite.Optimize.DEFAULT]
            |$converter.representative_dataset = \
            |    tf.lite.RepresentativeDataset($representativeDatasetGen)
            |$converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
            |$converter.inference_input_type = tf.uint8
            |$converter.inference_output_type = tf.uint8
            |
            |$tfliteModel = $converter.convert()
            |tf.gfile.GFile("$outputModelFilename", "wb").write($tfliteModel)
        """.trimMargin()
    }
}
