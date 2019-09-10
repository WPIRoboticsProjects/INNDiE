package edu.wpi.axon.core

import edu.wpi.axon.core.input.DictionaryInputStrategy
import edu.wpi.axon.core.input.InputStrategy
import edu.wpi.axon.core.loadimage.LoadImageFromDiskStrategy
import edu.wpi.axon.core.loadimage.LoadImageFromTensorProtoStrategy
import edu.wpi.axon.core.loadimage.LoadImageStrategy
import edu.wpi.axon.core.output.OutputStrategy
import edu.wpi.axon.core.output.Yolov3OutputStrategy
import edu.wpi.axon.core.postprocess.PostprocessStrategy
import edu.wpi.axon.core.postprocess.Yolov3PostprocessStrategy
import edu.wpi.axon.core.preprocess.ImagePreprocessStrategy
import edu.wpi.axon.core.preprocess.PilImagePreprocessStrategy
import org.junit.jupiter.api.Test

internal class TestMQP {

    private fun makeScript(
        modelPath: String,
        classesPath: String,
        loadImageStrategy: () -> String,
        imagePreprocessStrategy: ImagePreprocessStrategy,
        inputStrategy: InputStrategy,
        outputStrategy: OutputStrategy,
        postprocessStrategy: PostprocessStrategy
    ): String {
        val sessionInputsVariableName = "sessionInputs"

        val libraryFunctions = """
            |def letterbox_image(image, size):
            |    '''Resize image with unchanged aspect ratio using padding'''
            |    iw, ih = image.size
            |    w, h = size
            |    scale = min(w/iw, h/ih)
            |    nw = int(iw*scale)
            |    nh = int(ih*scale)
            |
            |    image = image.resize((nw,nh), Image.BICUBIC)
            |    new_image = Image.new('RGB', size, (128,128,128))
            |    new_image.paste(image, ((w-nw)//2, (h-nh)//2))
            |    return new_image
            |
            |def preprocess(img):
            |    model_image_size = (416, 416)
            |    boxed_image = letterbox_image(img, tuple(reversed(model_image_size)))
            |    image_data = np.array(boxed_image, dtype='float32')
            |    image_data /= 255.
            |    image_data = np.transpose(image_data, [2, 0, 1])
            |    image_data = np.expand_dims(image_data, 0)
            |    return image_data
            |
            |def postprocessYolov3(boxes, scores, indices):
            |    objects_identified = indices.shape[0]
            |    out_boxes, out_scores, out_classes = [], [], []
            |    if objects_identified > 0:
            |        for idx_ in indices:
            |            out_classes.append(classes[idx_[1]])
            |            out_scores.append(scores[tuple(idx_)])
            |            idx_1 = (idx_[0], idx_[2])
            |            out_boxes.append(boxes[idx_1])
            |        print(objects_identified, "objects identified in source image.")
            |    else:
            |        print("No objects identified in source image.")
            |    return out_boxes, out_scores, out_classes, objects_identified
            |
            ||def display_objdetect_image(image, out_boxes, out_classes, \
            |                            image_name='sample', objects_identified=None, save=True):
            |    plt.figure()
            |    fig, ax = plt.subplots(1, figsize=(12,9))
            |    ax.imshow(image)
            |    if objects_identified == None:
            |        objects_identified = len(out_boxes)
            |
            |    for i in range(objects_identified):
            |        y1, x1, y2, x2 = out_boxes[i]
            |        class_pred = out_classes[i]
            |        color = 'blue'
            |        box_h = (y2 - y1)
            |        box_w = (x2 - x1)
            |        bbox = patches.Rectangle((x1, y1), box_w, box_h, linewidth=2, edgecolor=color, facecolor='none')
            |        ax.add_patch(bbox)
            |        plt.text(x1, y1, s=class_pred, color='white', verticalalignment='top',
            |                 bbox={'color': color, 'pad': 0})
            |
            |    plt.axis('off')
            |    image_name = "images/"+image_name+"-det.jpg"
            |    plt.savefig(image_name, bbox_inches='tight', pad_inches=0.0)
            |    if save:
            |        plt.show()
            |    else:
            |        img = imread(image_name)
            |        os.remove(image_name)
            |        return img
        """.trimMargin()

        return """
            |import onnx
            |import onnxruntime
            |import numpy as np
            |from onnx import numpy_helper
            |
            |from PIL import Image
            |import matplotlib.pyplot as plt
            |import matplotlib.patches as patches
            |import time
            |
            |%matplotlib inline
            |
            |$libraryFunctions
            |
            |session = onnxruntime.InferenceSession('$modelPath')
            |classes = [line.rstrip('\n') for line in open('$classesPath')]
            |${loadImageStrategy()}
            |
            |${imagePreprocessStrategy.getPythonSegment()}
            |
            |$sessionInputsVariableName = session.get_inputs()
            |${outputStrategy.getPythonSegment()} = session.run(None, ${
        inputStrategy.getPythonSegment(sessionInputsVariableName)})
            |
            |out_boxes, out_scores, out_classes, objects_identified = ${postprocessStrategy.getPythonSegment()}
        """.trimMargin()
    }

    private fun makeYolov3Script(
        modelPath: String,
        classesPath: String,
        loadImageStrategy: LoadImageStrategy,
        imagePath: String? = null
    ): String {
        val imageVariableName = "img"
        val imageDataVariableName = "img_data"
        val imageSizeVariableName = "img_size"
        val boxesVariableName = "boxes"
        val scoresVariableName = "scores"
        val indicesVariableName = "indices"

        val yolov3ImagePreprocessStrategy = PilImagePreprocessStrategy(
            imageVariableName,
            imageDataVariableName,
            imageSizeVariableName
        )

        // val yolov3ImagePreprocessStrategy = TensorProtoImagePreprocessStrategy(
        //     imageVariableName,
        //     imageDataVariableName,
        //     imageSizeVariableName
        // )

        val dictionaryInputStrategy = DictionaryInputStrategy(
            imageDataVariableName,
            imageSizeVariableName
        )

        val yolov3OutputStrategy = Yolov3OutputStrategy(
            boxesVariableName,
            scoresVariableName,
            indicesVariableName
        )

        val yolov3PostprocessStrategy = Yolov3PostprocessStrategy(
            boxesVariableName,
            scoresVariableName,
            indicesVariableName
        )

        val imageName = imagePath?.substringAfterLast("/")?.substringBeforeLast(".")

        return """
            |${makeScript(
            modelPath = modelPath,
            classesPath = classesPath,
            loadImageStrategy = { loadImageStrategy.getPythonSegment(imageVariableName) },
            imagePreprocessStrategy = yolov3ImagePreprocessStrategy,
            inputStrategy = dictionaryInputStrategy,
            outputStrategy = yolov3OutputStrategy,
            postprocessStrategy = yolov3PostprocessStrategy
        )}
            |
            |${if (imageName != null) """display_objdetect_image(img, out_boxes, out_classes, "$imageName")""" else ""}
        """.trimMargin()
    }

    private fun makeResnet50Script(
        modelPath: String,
        classesPath: String,
        loadImageStrategy: LoadImageStrategy,
        imagePath: String? = null
    ): String {
        val imageVariableName = "img"
        val imageDataVariableName = "img_data"
        val imageSizeVariableName = "img_size"
        val boxesVariableName = "boxes"
        val scoresVariableName = "scores"
        val indicesVariableName = "indices"

        val yolov3ImagePreprocessStrategy = PilImagePreprocessStrategy(
            imageVariableName,
            imageDataVariableName,
            imageSizeVariableName
        )

        val dictionaryInputStrategy = DictionaryInputStrategy(
            imageDataVariableName,
            imageSizeVariableName
        )

        val yolov3OutputStrategy = Yolov3OutputStrategy(
            boxesVariableName,
            scoresVariableName,
            indicesVariableName
        )

        val yolov3PostprocessStrategy = Yolov3PostprocessStrategy(
            boxesVariableName,
            scoresVariableName,
            indicesVariableName
        )

        val imageName = imagePath?.substringAfterLast("/")?.substringBeforeLast(".")

        return """
            |${makeScript(
            modelPath = modelPath,
            classesPath = classesPath,
            loadImageStrategy = { loadImageStrategy.getPythonSegment(imageVariableName) },
            imagePreprocessStrategy = yolov3ImagePreprocessStrategy,
            inputStrategy = dictionaryInputStrategy,
            outputStrategy = yolov3OutputStrategy,
            postprocessStrategy = yolov3PostprocessStrategy
        )}
            |
            |${if (imageName != null) """display_objdetect_image(img, out_boxes, out_classes, "$imageName")""" else ""}
        """.trimMargin()
    }

    @Test
    fun `make yolov3 script`() {
        val imagePath = "images/horses.jpg"

        val script = makeYolov3Script(
            modelPath = "yolov3.onnx",
            classesPath = "coco_classes.txt",
            loadImageStrategy = LoadImageFromDiskStrategy(imagePath),
            imagePath = imagePath
        )

        println(script)
    }

    @Test
    fun `make yolov3 script with tensor`() {
        val script = makeYolov3Script(
            modelPath = "yolov3.onnx",
            classesPath = "coco_classes.txt",
            loadImageStrategy = LoadImageFromTensorProtoStrategy("resnet50v2/test_data_set_0/input_0.pb")
        )

        println(script)
    }

    @Test
    fun `make resnet50 script`() {
        val imagePath = "images/horses.jpg"

        val script = makeResnet50Script(
            modelPath = "resnet50.onnx",
            classesPath = "resnet50_classes.txt",
            loadImageStrategy = LoadImageFromDiskStrategy(imagePath),
            imagePath = imagePath
        )

        println(script)
    }
}
