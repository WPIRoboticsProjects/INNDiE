package edu.wpi.axon.core.dsl

import edu.wpi.axon.core.dsl.container.DefaultTaskContainer
import edu.wpi.axon.core.dsl.container.DefaultVariableContainer
import edu.wpi.axon.core.dsl.task.InferenceTask
import edu.wpi.axon.core.dsl.task.InferenceTaskOutput
import edu.wpi.axon.core.dsl.task.YoloV3PostprocessTask
import edu.wpi.axon.core.dsl.task.Yolov3PostprocessOutput
import edu.wpi.axon.core.dsl.variable.ClassLabels
import edu.wpi.axon.core.dsl.variable.ImageInputData
import edu.wpi.axon.core.dsl.variable.InferenceSession
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

internal class ScriptGeneratorDslIntegrationTest : KoinTest {

    @AfterEach
    fun afterEach() {
        stopKoin()
    }

    @Test
    fun `integration test`() {
        startKoin {
            modules(module {
                single<VariableNameValidator> { PythonVariableNameValidator() }
                single<PathValidator> { DefaultPathValidator() }
            })
        }

        val dsl = ScriptGeneratorDsl(DefaultVariableContainer.of(), DefaultTaskContainer.of()) {
            val session by variables.creating(InferenceSession::class) {
                modelPath = "/model.onnx"
            }

            val classes by variables.creating(ClassLabels::class) {
                path = "/classes.txt"
            }

            val inputData by variables.creating(ImageInputData::class) {
                path = "/image.png"
            }

            val inferenceOutput by variables.creating(InferenceTaskOutput::class)
            val inferenceTask by tasks.running(InferenceTask::class) {
                input = inputData
                inferenceSession = session
                output = inferenceOutput
            }

            val postProcessedOutput by variables.creating(Yolov3PostprocessOutput::class)
            val postProcessTask by tasks.running(YoloV3PostprocessTask::class) {
                input = inferenceOutput
                output = postProcessedOutput
            }

            scriptOutput(postProcessedOutput)
        }
    }
}
