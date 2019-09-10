package edu.wpi.axon.tasks.yolov3

import edu.wpi.axon.dsl.alwaysValidImportValidator
import edu.wpi.axon.dsl.alwaysValidPathValidator
import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.testutil.KoinTestFixture
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal class YoloV3PostprocessTaskTest : KoinTestFixture() {

    @Test
    fun `test code`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
                alwaysValidPathValidator()
            })
        }

        val task = YoloV3PostprocessTask("task").apply {
            input = configuredCorrectly("var1")
            output = configuredCorrectly("var2")
        }

        task.code() shouldBe "var2 = postprocessYoloV3(var1)"
    }
}
