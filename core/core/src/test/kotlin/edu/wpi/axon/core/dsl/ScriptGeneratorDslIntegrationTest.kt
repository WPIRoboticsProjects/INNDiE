package edu.wpi.axon.core.dsl

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import edu.wpi.axon.core.dsl.container.DefaultTaskContainer
import edu.wpi.axon.core.dsl.container.DefaultVariableContainer
import edu.wpi.axon.core.dsl.task.InferenceTask
import edu.wpi.axon.core.dsl.task.InferenceTaskOutput
import edu.wpi.axon.core.dsl.task.YoloV3PostprocessTask
import edu.wpi.axon.core.dsl.task.Yolov3PostprocessOutput
import edu.wpi.axon.core.dsl.validator.path.DefaultPathValidator
import edu.wpi.axon.core.dsl.validator.path.PathValidator
import edu.wpi.axon.core.dsl.validator.variablename.PythonVariableNameValidator
import edu.wpi.axon.core.dsl.validator.variablename.VariableNameValidator
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

    @SuppressWarnings("LongMethod")
    @Test
    fun `run yolov3 model`() {
        startKoin {
            modules(module {
                single<VariableNameValidator> { PythonVariableNameValidator() }
                single<PathValidator> { DefaultPathValidator() }
            })
        }

        @Suppress("UNUSED_VARIABLE")
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

            scriptOutput = postProcessedOutput
        }

        assertThat(
            dsl.computeImports(), equalTo(
                setOf(
                    Import.ModuleOnly("onnxruntime"),
                    Import.ModuleAndIdentifier("PIL", "Image"),
                    Import.ModuleOnly("onnx"),
                    Import.ModuleAndIdentifier("axon", "postprocessYolov3")
                )
            )
        )
    }
}
