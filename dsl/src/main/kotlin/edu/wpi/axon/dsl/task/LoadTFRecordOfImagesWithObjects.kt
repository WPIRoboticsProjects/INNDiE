package edu.wpi.axon.dsl.task

import arrow.core.None
import arrow.core.Option
import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.UniqueVariableNameGenerator
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.util.singleAssign
import org.koin.core.inject

/**
 * Loads a custom dataset from a TFRecord.
 */
class LoadTFRecordOfImagesWithObjects(name: String) : BaseTask(name) {

    /**
     * The dataset to load.
     */
    var dataset: Dataset.Custom by singleAssign()

    /**
     * The name of the S3 bucket to download the dataset from.
     */
    var bucketName: String by singleAssign()

    /**
     * The region.
     */
    var region: Option<String> = None

    /**
     * The x-axis data.
     */
    var xOutput: Variable by singleAssign()

    /**
     * The y-axis data.
     */
    var yOutput: Variable by singleAssign()

    override val imports: Set<Import> = setOf(
        makeImport("import tensorflow as tf"),
        makeImport("import os"),
        makeImport("import numpy as np")
    )

    override val inputs: Set<Variable> = setOf()

    override val outputs: Set<Variable>
        get() = setOf(xOutput, yOutput)

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    private val variableNameGenerator: UniqueVariableNameGenerator by inject()

    override fun code(): String {
        val classes = variableNameGenerator.uniqueVariableName()
        val datasetVar = variableNameGenerator.uniqueVariableName()
        val feature = variableNameGenerator.uniqueVariableName()
        val parsedDataset = variableNameGenerator.uniqueVariableName()
        val parseClasses = variableNameGenerator.uniqueVariableName()
        val allLabels = variableNameGenerator.uniqueVariableName()
        val allImages = variableNameGenerator.uniqueVariableName()
        val allIndices = variableNameGenerator.uniqueVariableName()
        val imgHeight = variableNameGenerator.uniqueVariableName()
        val imgWidth = variableNameGenerator.uniqueVariableName()
        val imgDepth = variableNameGenerator.uniqueVariableName()
        // TODO: Don't require eager execution
        // TODO: Load bounding boxes
        return """
            |assert tf.executing_eagerly()
            |
            |def $parseClasses(dataset):
            |    with open(dataset + "/meta.json", "r") as f:
            |        data = json.loads(f.readlines()[0])
            |        return [label["title"] for label in data["classes"]]
            |
            |$classes = $parseClasses("${dataset.baseNameWithoutExtension}")
            |$datasetVar = tf.data.TFRecordDataset(os.path.join("${dataset.baseNameWithoutExtension}", "${dataset.baseNameWithoutExtension + ".record"}"))
            |
            |$feature = {
            |    "image/filename": tf.FixedLenFeature([], tf.string),
            |    "image/image_raw": tf.FixedLenFeature([], tf.string),
            |    "image/height": tf.FixedLenFeature([], tf.int64),
            |    "image/width": tf.FixedLenFeature([], tf.int64),
            |    "image/depth": tf.FixedLenFeature([], tf.int64),
            |    "image/bbox/xmin": tf.FixedLenFeature([], tf.int64),
            |    "image/bbox/xmax": tf.FixedLenFeature([], tf.int64),
            |    "image/bbox/ymin": tf.FixedLenFeature([], tf.int64),
            |    "image/bbox/ymax": tf.FixedLenFeature([], tf.int64),
            |    "image/label": tf.FixedLenFeature([], tf.string)
            |}
            |
            |$parsedDataset = $datasetVar.map(lambda it: tf.io.parse_single_example(it, $feature))
            |for feat in $parsedDataset:
            |    $imgHeight = feat["image/height"].numpy()
            |    $imgWidth = feat["image/width"].numpy()
            |    $imgDepth = feat["image/depth"].numpy()
            |    break
            |
            |$allLabels = np.array([])
            |$allImages = np.array([])
            |for feat in $parsedDataset:
            |    if $imgHeight != feat["image/height"].numpy() or $imgWidth != feat["image/width"].numpy() or $imgDepth != feat["image/depth"].numpy():
            |        raise ValueError("Image dimensions did not match.")
            |    $allLabels = np.append($allLabels, tf.cast(feat["image/label"], tf.string))
            |    img = tf.image.decode_jpeg(feat["image/image_raw"])
            |    img = tf.image.resize(img, [feat["image/height"], feat["image/width"]])
            |    img = np.array(img)
            |    $allImages = np.append($allImages, img)
            |
            |$allIndices = np.array(list(map(lambda it: $classes.index(it.decode()), $allLabels)))
            |# all_indices_onehot = np.array(tf.one_hot($allIndices, len($classes)))
            |
            |$allImages = $allImages.reshape(-1, $imgHeight, $imgWidth, $imgDepth)
            |
            |${xOutput.name} = $allImages
            |${yOutput.name} = $allIndices
        """.trimMargin()
    }
}
