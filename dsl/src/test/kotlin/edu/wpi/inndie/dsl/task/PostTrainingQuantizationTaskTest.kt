package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.alwaysValidImportValidator
import edu.wpi.inndie.dsl.alwaysValidPathValidator
import edu.wpi.inndie.dsl.configuredCorrectly
import edu.wpi.inndie.dsl.mockVariableNameGenerator
import edu.wpi.inndie.testutil.KoinTestFixture
import io.kotlintest.matchers.booleans.shouldBeFalse
import io.kotlintest.matchers.booleans.shouldBeTrue
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal class PostTrainingQuantizationTaskTest : KoinTestFixture() {

    @Test
    fun `test non-h5 input model`() {
        startKoin {
            modules(
                module {
                    alwaysValidImportValidator()
                    alwaysValidPathValidator()
                }
            )
        }

        PostTrainingQuantizationTask("").apply {
            modelFilename = "input.invalid"
            outputModelFilename = "output.tflite"
            representativeDataset = configuredCorrectly("representative_dataset")
        }.isConfiguredCorrectly().shouldBeFalse()
    }

    @Test
    fun `test non-tflite output model`() {
        startKoin {
            modules(
                module {
                    alwaysValidImportValidator()
                    alwaysValidPathValidator()
                }
            )
        }

        PostTrainingQuantizationTask("").apply {
            modelFilename = "input.h5"
            outputModelFilename = "output.invalid"
            representativeDataset = configuredCorrectly("representative_dataset")
        }.isConfiguredCorrectly().shouldBeFalse()
    }

    @Test
    fun `test code gen`() {
        startKoin {
            modules(
                module {
                    alwaysValidImportValidator()
                    alwaysValidPathValidator()
                    mockVariableNameGenerator()
                }
            )
        }

        val task = PostTrainingQuantizationTask("").apply {
            modelFilename = "input.h5"
            outputModelFilename = "output.tflite"
            representativeDataset = configuredCorrectly("representative_dataset")
        }

        task.isConfiguredCorrectly().shouldBeTrue()
        task.code().shouldBe(
            """
            |def var1():
            |    for var2 in representative_dataset:
            |        yield [[var2]]
            |
            |var3 = tf.lite.TFLiteConverter.from_keras_model_file("input.h5")
            |var3.optimizations = [tf.lite.Optimize.DEFAULT]
            |var3.representative_dataset = \
            |    tf.lite.RepresentativeDataset(var1)
            |var3.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
            |var3.inference_input_type = tf.uint8
            |var3.inference_output_type = tf.uint8
            |
            |var4 = var3.convert()
            |tf.gfile.GFile("output.tflite", "wb").write(var4)
            """.trimMargin()
        )
    }
}
