package edu.wpi.axon.dsl.task

import arrow.core.None
import arrow.core.Option
import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.UniqueVariableNameGenerator
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.code.pythonString
import edu.wpi.axon.util.singleAssign
import org.koin.core.inject

/**
 * Loads a custom dataset.
 */
class LoadSuperviselyDataset(name: String) : BaseTask(name) {

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
        makeImport("import axon.client"),
        makeImport("import json"),
        makeImport("import pathlib"),
        makeImport("import os"),
        makeImport("import itertools"),
        makeImport("import ntpath"),
        makeImport("import numpy as np")
    )

    override val inputs: Set<Variable> = setOf()

    override val outputs: Set<Variable>
        get() = setOf(xOutput, yOutput)

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    override fun isConfiguredCorrectly() =
        dataset.pathInS3.endsWith(".tar") && super.isConfiguredCorrectly()

    private val variableNameGenerator: UniqueVariableNameGenerator by inject()

    @Suppress("DuplicatedCode")
    override fun code(): String {
        val classes = variableNameGenerator.uniqueVariableName()
        val datasetVar = variableNameGenerator.uniqueVariableName()
        val feature = variableNameGenerator.uniqueVariableName()
        val parsedDataset = variableNameGenerator.uniqueVariableName()
        val bytesFeature = variableNameGenerator.uniqueVariableName()
        val floatFeature = variableNameGenerator.uniqueVariableName()
        val int64Feature = variableNameGenerator.uniqueVariableName()
        val parseClasses = variableNameGenerator.uniqueVariableName()
        val convertToRecord = variableNameGenerator.uniqueVariableName()
        val allLabels = variableNameGenerator.uniqueVariableName()
        val allImages = variableNameGenerator.uniqueVariableName()
        val allIndices = variableNameGenerator.uniqueVariableName()
        val imgHeight = variableNameGenerator.uniqueVariableName()
        val imgWidth = variableNameGenerator.uniqueVariableName()
        val imgDepth = variableNameGenerator.uniqueVariableName()
        // TODO: Don't hardcode depth as 3
        // TODO: Support resizing images to a desired size using decode_jpeg
        // TODO: Support downscaling images using decode_jpeg
        // TODO: Don't require eager execution
        // TODO: Don't convert this each time, cache it in S3
        return """
            |def $bytesFeature(value):
            |    if isinstance(value, type(tf.constant(0))):
            |        value = value.numpy()
            |    return tf.train.Feature(bytes_list=tf.train.BytesList(value=[value]))
            |
            |def $floatFeature(value):
            |    return tf.train.Feature(float_list=tf.train.FloatList(value=[value]))
            |
            |def $int64Feature(value):
            |    return tf.train.Feature(int64_list=tf.train.Int64List(value=[value]))
            |
            |def $parseClasses(dataset):
            |    with open(dataset + "/meta.json", "r") as f:
            |        data = json.loads(f.readlines()[0])
            |        return [label["title"] for label in data["classes"]]
            |
            |def $convertToRecord(dataset, record_filename):
            |    writer = tf.python_io.TFRecordWriter(os.path.join(dataset, record_filename))
            |    data_dirs = [os.path.join(dataset, subdir) for subdir in os.listdir(dataset)
            |                 if os.path.isdir(os.path.join(dataset, subdir))]
            |    print(data_dirs)
            |    ann_files = [pathlib.Path(it).rglob("*.json") for it in data_dirs]
            |    for file in ann_files:
            |        for ann in file:
            |            with open(ann, "r") as ann_file:
            |                data = json.loads(ann_file.readlines()[0])
            |                for obj in data["objects"]:
            |                    p1, p2 = obj["points"]["exterior"]
            |                    x1, x2 = sorted([p1[0], p2[0]])
            |                    y1, y2 = sorted([p1[1], p2[1]])
            |                    head, tail = str(ann).split("ann")
            |                    img_path = head + "img" + os.path.splitext(tail)[0]
            |                    img_width = data["size"]["width"]
            |                    img_height = data["size"]["height"]
            |                    img_depth = 3
            |                    label = obj["classTitle"]
            |
            |                    feature = {
            |                        "image/filename": $bytesFeature(bytes(img_path, "utf-8")),
            |                        "image/image_raw": $bytesFeature(open(img_path, "rb").read()),
            |                        "image/height": $int64Feature(img_height),
            |                        "image/width": $int64Feature(img_width),
            |                        "image/depth": _int64_feature(img_depth),
            |                        "image/bbox/xmin": $int64Feature(x1),
            |                        "image/bbox/xmax": $int64Feature(x2),
            |                        "image/bbox/ymin": $int64Feature(y1),
            |                        "image/bbox/ymax": $int64Feature(y2),
            |                        "image/label": $bytesFeature(bytes(label, "utf-8"))
            |                    }
            |
            |                    writer.write(tf.train.Example(
            |                        features=tf.train.Features(feature=feature)).SerializeToString())
            |    writer.close()
            |
            |axon.client.impl_download_dataset(${dataset.pathInS3}, $bucketName, ${pythonString(
            region
        )})
            |with f as tarfile.open(${dataset.baseNameWithExtension}):
            |   f.extractall()
            |$classes = $parseClasses(${dataset.baseNameWithoutExtension})
            |$convertToRecord(${dataset.baseNameWithoutExtension}, ${dataset.baseNameWithoutExtension + ".record"})
            |assert tf.executing_eagerly()
            |$datasetVar = tf.data.TFRecordDataset(os.path.join(${dataset.baseNameWithoutExtension}, ${dataset.baseNameWithoutExtension + ".record"}))
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
            |for feat in parsed_dataset:
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
            |$allIndices = np.array(list(map(lambda it: classes.index(it.decode()), $allLabels)))
            |# all_indices_onehot = np.array(tf.one_hot($allIndices, len(classes)))
            |
            |$allImages = $allImages.reshape(-1, $imgHeight, $imgWidth, $imgDepth)
            |
            |$xOutput = $allImages
            |$yOutput = $allIndices
        """.trimMargin()
    }
}
