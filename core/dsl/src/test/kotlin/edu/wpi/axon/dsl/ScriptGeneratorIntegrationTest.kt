package edu.wpi.axon.dsl

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import edu.wpi.axon.dsl.container.DefaultPolymorphicNamedDomainObjectContainer
import edu.wpi.axon.dsl.task.InferenceTask
import edu.wpi.axon.dsl.task.LoadClassLabels
import edu.wpi.axon.dsl.task.LoadImageTask
import edu.wpi.axon.dsl.task.MakeNewInferenceSession
import edu.wpi.axon.dsl.task.yolov3.ConstructYoloV3ImageInput
import edu.wpi.axon.dsl.task.yolov3.LoadYoloV3ImageData
import edu.wpi.axon.dsl.task.yolov3.YoloV3PostprocessTask
import edu.wpi.axon.dsl.validator.path.DefaultPathValidator
import edu.wpi.axon.dsl.validator.path.PathValidator
import edu.wpi.axon.dsl.validator.variablename.PythonVariableNameValidator
import edu.wpi.axon.dsl.validator.variablename.VariableNameValidator
import edu.wpi.axon.dsl.variable.Variable
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import java.util.concurrent.CountDownLatch

@Suppress("UNUSED_VARIABLE")
@SuppressWarnings("LargeClass")
internal class ScriptGeneratorIntegrationTest : KoinTest {

    @AfterEach
    fun afterEach() {
        stopKoin()
    }

    @SuppressWarnings("LongMethod", "MaxLineLength")
    @Test
    fun `run yolov3 model`() {
        startKoin {
            modules(module {
                single<VariableNameValidator> { PythonVariableNameValidator() }
                single<PathValidator> { DefaultPathValidator() }
            })
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
            |from axon import postprocessYolov3
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

    @Test
    fun `code dependencies should be called`() {
        startKoin {
            modules(module {
                single<VariableNameValidator> { PythonVariableNameValidator() }
                single<PathValidator> { DefaultPathValidator() }
            })
        }

        val codeLatch = CountDownLatch(2)
        ScriptGenerator(
            DefaultPolymorphicNamedDomainObjectContainer.of(),
            DefaultPolymorphicNamedDomainObjectContainer.of()
        ) {
            val task1 by tasks.running(MockTask::class) {
                latch = codeLatch
            }

            val task2 by tasks.running(MockTask::class) {
                latch = codeLatch
                dependencies += task1
            }

            lastTask = task2
        }.code()

        assertThat(codeLatch.count, equalTo(0L))
    }

    @Test
    fun `tasks are not run multiple times`() {
        startKoin {
            modules(module {
                single<VariableNameValidator> { PythonVariableNameValidator() }
                single<PathValidator> { DefaultPathValidator() }
            })
        }

        val codeLatch = CountDownLatch(3)
        ScriptGenerator(
            DefaultPolymorphicNamedDomainObjectContainer.of(),
            DefaultPolymorphicNamedDomainObjectContainer.of()
        ) {
            val task1 by tasks.running(MockTask::class) {
                latch = codeLatch
            }

            val task2 by tasks.running(MockTask::class) {
                latch = codeLatch
                dependencies += task1
            }

            lastTask = task2
        }.code()

        assertThat(codeLatch.count, equalTo(1L))
    }

    @Test
    fun `two tasks that depend on the same task does not duplicate code gen`() {
        startKoin {
            modules(module {
                single<VariableNameValidator> { PythonVariableNameValidator() }
                single<PathValidator> { DefaultPathValidator() }
            })
        }

        val task1CodeLatch = CountDownLatch(2)
        ScriptGenerator(
            DefaultPolymorphicNamedDomainObjectContainer.of(),
            DefaultPolymorphicNamedDomainObjectContainer.of()
        ) {
            val task1 by tasks.running(MockTask::class) {
                latch = task1CodeLatch
            }
            val task2 by tasks.running(MockTask::class) {
                dependencies += task1
            }
            val task3 by tasks.running(MockTask::class) {
                dependencies += task1
            }
            lastTask = task3
        }.code()

        assertThat(task1CodeLatch.count, equalTo(1L))
    }

    @Test
    fun `two tasks that depend on the same task linked by a variable does not duplicate code gen`() {
        startKoin {
            modules(module {
                single<VariableNameValidator> { PythonVariableNameValidator() }
                single<PathValidator> { DefaultPathValidator() }
            })
        }

        val task1CodeLatch = CountDownLatch(2)
        ScriptGenerator(
            DefaultPolymorphicNamedDomainObjectContainer.of(),
            DefaultPolymorphicNamedDomainObjectContainer.of()
        ) {
            val task1Var by variables.creating(Variable::class)
            val task1 by tasks.running(MockTask::class) {
                latch = task1CodeLatch
                outputs += task1Var
            }

            val task2 by tasks.running(MockTask::class) {
                inputs += task1Var
            }

            val task3 by tasks.running(MockTask::class) {
                inputs += task1Var
                dependencies += task2
            }

            lastTask = task3
        }.code()

        assertThat(task1CodeLatch.count, equalTo(1L))
    }
}
