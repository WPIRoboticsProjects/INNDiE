package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.UniqueVariableNameGenerator
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.makeImport
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.util.singleAssign
import org.koin.core.inject

/**
 * Converts a Supervisely dataset to a TFRecord.
 */
class ConvertSuperviselyDatasetToRecord(name: String) : BaseTask(name) {

    /**
     * The dataset to load.
     */
    var dataset: Dataset.Custom by singleAssign()

    override val imports: Set<Import> = setOf(
        makeImport("import tensorflow as tf"),
        makeImport("import json"),
        makeImport("import pathlib"),
        makeImport("import os"),
        makeImport("import ntpath"),
        makeImport("import numpy as np"),
        makeImport("import tarfile")
    )

    override val inputs: Set<Variable> = setOf()

    override val outputs: Set<Variable> = setOf()

    override val dependencies: MutableSet<Code<*>> = mutableSetOf()

    override fun isConfiguredCorrectly() =
        dataset.path.path.endsWith(".tar") && super.isConfiguredCorrectly()

    private val variableNameGenerator: UniqueVariableNameGenerator by inject()

    override fun code(): String {
        val bytesFeature = variableNameGenerator.uniqueVariableName()
        val floatFeature = variableNameGenerator.uniqueVariableName()
        val int64Feature = variableNameGenerator.uniqueVariableName()
        val convertToRecord = variableNameGenerator.uniqueVariableName()
        // TODO: Don't hardcode depth as 3
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
            |                        "image/depth": $int64Feature(img_depth),
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
            |with tarfile.open("${dataset.baseNameWithExtension}") as f:
            |    f.extractall()
            |
            |$convertToRecord('${dataset.baseNameWithoutExtension}', "${dataset.baseNameWithoutExtension + ".record"}")
        """.trimMargin()
    }
}
