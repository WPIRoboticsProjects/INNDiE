package edu.wpi.axon.dsl

import edu.wpi.axon.dsl.task.ApplyLayerDeltaTask
import edu.wpi.axon.dsl.task.InferenceTask
import edu.wpi.axon.dsl.task.LoadClassLabels
import edu.wpi.axon.dsl.task.LoadImageTask
import edu.wpi.axon.dsl.task.MakeNewInferenceSession
import edu.wpi.axon.dsl.task.Task
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.tasks.yolov3.ConstructYoloV3ImageInput
import edu.wpi.axon.tasks.yolov3.LoadYoloV3ImageData
import edu.wpi.axon.tasks.yolov3.YoloV3PostprocessTask
import edu.wpi.axon.testutil.KoinTestFixture
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.shouldThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.core.context.startKoin
import org.koin.dsl.module
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

internal class GeneralTaskConfigurationTest : KoinTestFixture() {

    @ParameterizedTest
    @MethodSource("tasksSource")
    fun `test uninitialized input`(
        getTask: () -> Task,
        properties: List<KProperty1<Task, Variable>>
    ) {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
                alwaysValidPathValidator()
            })
        }

        properties.forEach { uninitializedProp ->
            val task = getTask()

            // Don't initialize the uninitializedProp. Just initialize everything else
            properties.filter { it != uninitializedProp }.forEach { validProp ->
                (validProp as KMutableProperty1).set(task, configuredCorrectly())
            }

            shouldThrow<UninitializedPropertyAccessException> { task.isConfiguredCorrectly() }
        }
    }

    @ParameterizedTest
    @MethodSource("tasksSource")
    fun `test incorrectly configured input`(
        getTask: () -> Task,
        properties: List<KProperty1<Task, Variable>>
    ) {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
                alwaysValidPathValidator()
            })
        }

        properties.forEach { invalidProp ->
            val task = getTask()

            properties.filter { it != invalidProp }.forEach { validProp ->
                (validProp as KMutableProperty1).set(task, configuredCorrectly())
            }

            (invalidProp as KMutableProperty1).set(task, configuredIncorrectly())

            task.isConfiguredCorrectly().shouldBeFalse()
        }
    }

    companion object {

        @Suppress("unused")
        @JvmStatic
        fun tasksSource() = listOf(
            Arguments.of(
                { InferenceTask("") },
                listOf(InferenceTask::input, InferenceTask::inferenceSession, InferenceTask::output)
            ),
            Arguments.of(
                { LoadClassLabels("").apply { classLabelsPath = "" } },
                listOf(LoadClassLabels::classOutput)
            ),
            Arguments.of(
                { LoadImageTask("").apply { imagePath = "" } },
                listOf(LoadImageTask::imageOutput)
            ),
            Arguments.of(
                { MakeNewInferenceSession("").apply { modelPathInput = "" } },
                listOf(MakeNewInferenceSession::sessionOutput)
            ),
            Arguments.of(
                { ConstructYoloV3ImageInput("") },
                listOf(
                    ConstructYoloV3ImageInput::imageDataInput,
                    ConstructYoloV3ImageInput::imageSizeInput,
                    ConstructYoloV3ImageInput::sessionInput,
                    ConstructYoloV3ImageInput::output
                )
            ),
            Arguments.of(
                { LoadYoloV3ImageData("") },
                listOf(
                    LoadYoloV3ImageData::imageInput,
                    LoadYoloV3ImageData::imageDataOutput,
                    LoadYoloV3ImageData::imageSizeOutput
                )
            ),
            Arguments.of(
                { YoloV3PostprocessTask("") },
                listOf(YoloV3PostprocessTask::input, YoloV3PostprocessTask::output)
            ),
            Arguments.of(
                { ApplyLayerDeltaTask("") },
                listOf(ApplyLayerDeltaTask::modelInput, ApplyLayerDeltaTask::newModelOutput)
            )
        )
    }
}
