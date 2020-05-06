package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.alwaysValidPathValidator
import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.inndie.testutil.KoinTestFixture
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal class LoadModelTaskTest : KoinTestFixture() {

    @Test
    fun `test code gen`() {
        startKoin {
            modules(module {
                alwaysValidPathValidator()
            })
        }

        val task = LoadModelTask("task").apply {
            modelPath = "model.h5"
            modelOutput = configuredCorrectly("output")
        }

        task.code() shouldBe """
            |output = tf.keras.models.load_model("model.h5")
        """.trimMargin()
    }
}
