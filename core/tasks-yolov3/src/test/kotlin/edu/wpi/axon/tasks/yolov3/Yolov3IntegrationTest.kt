package edu.wpi.axon.tasks.yolov3

import edu.wpi.axon.dsl.ScriptGenerator
import edu.wpi.axon.dsl.container.DefaultPolymorphicNamedDomainObjectContainer
import edu.wpi.axon.dsl.creating
import edu.wpi.axon.dsl.defaultModule
import edu.wpi.axon.dsl.running
import edu.wpi.axon.dsl.task.InferenceTask
import edu.wpi.axon.dsl.task.LoadClassLabels
import edu.wpi.axon.dsl.task.LoadImageTask
import edu.wpi.axon.dsl.task.MakeNewInferenceSession
import edu.wpi.axon.dsl.variable.Variable
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest

@Suppress("UNUSED_VARIABLE")
@SuppressWarnings("LargeClass")
internal class Yolov3IntegrationTest : KoinTest {

    @AfterEach
    fun afterEach() {
        stopKoin()
    }

    @SuppressWarnings("LongMethod", "MaxLineLength")
    @Test
    fun `generate yolov3 run script`() {
        startKoin {
            modules(defaultModule())
        }

        val dsl = ScriptGenerator(
            DefaultPolymorphicNamedDomainObjectContainer.of(),
            DefaultPolymorphicNamedDomainObjectContainer.of()
        ) {
            val session by variables.creating(Variable::class)
            val makeNewInferenceSession by tasks.running(MakeNewInferenceSession::class) {
                modelPathInput = "yolov3.onnx"
                sessionOutput = session
            }

            val classes by variables.creating(Variable::class)
            val loadClassLabels by tasks.running(LoadClassLabels::class) {
                classLabelsPath = "coco_classes.txt"
                classOutput = classes
            }

            requireGeneration(classes)

            val loadedImage by variables.creating(Variable::class)
            val loadImageTask by tasks.running(LoadImageTask::class) {
                imagePath = "horses.jpg"
                imageOutput = loadedImage
            }

            val imageData by variables.creating(Variable::class)
            val imageSize by variables.creating(Variable::class)
            val loadImageData by tasks.running(LoadYoloV3ImageData::class) {
                imageInput = loadedImage
                imageDataOutput = imageData
                imageSizeOutput = imageSize
            }

            val inferenceInput by variables.creating(Variable::class)
            val makeYolov3Input by tasks.running(ConstructYoloV3ImageInput::class) {
                imageDataInput = imageData
                imageSizeInput = imageSize
                sessionInput = session
                output = inferenceInput
            }

            val inferenceOutput by variables.creating(Variable::class)
            val inferenceTask by tasks.running(InferenceTask::class) {
                input = inferenceInput
                inferenceSession = session
                output = inferenceOutput
            }

            val postProcessedOutput by variables.creating(Variable::class)
            val postProcessTask by tasks.running(YoloV3PostprocessTask::class) {
                input = inferenceOutput
                output = postProcessedOutput
            }

            // scriptOutput = postProcessedOutput
            lastTask = postProcessTask
        }

        val code = dsl.code()
        assertEquals(
            """
            |from PIL import Image
            |from axon import postprocessYoloV3
            |from axon import preprocessYoloV3
            |import numpy as np
            |import onnx
            |import onnxruntime
            |
            |session = onnxruntime.InferenceSession('yolov3.onnx')
            |
            |loadedImage = Image.open('horses.jpg')
            |
            |imageData = preprocessYoloV3(loadedImage)
            |imageSize = np.array([loadedImage.size[1], loadedImage.size[0]], dtype=np.float32).reshape(1, 2)
            |
            |inferenceInput = {session.get_inputs()[0].name: imageData, session.get_inputs()[1].name: imageSize}
            |
            |inferenceOutput = session.run(None, inferenceInput)
            |
            |postProcessedOutput = postprocessYoloV3(inferenceOutput)
            |
            |
            |classes = [line.rstrip('\n') for line in open('coco_classes.txt')]
            """.trimMargin(),
            code
        )
    }
}
