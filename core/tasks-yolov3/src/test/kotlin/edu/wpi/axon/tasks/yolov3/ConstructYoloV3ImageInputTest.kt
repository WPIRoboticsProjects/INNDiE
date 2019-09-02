package edu.wpi.axon.tasks.yolov3

import edu.wpi.axon.dsl.alwaysValidImportValidator
import edu.wpi.axon.dsl.alwaysValidPathValidator
import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.testutil.KoinTestFixture
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal class ConstructYoloV3ImageInputTest : KoinTestFixture() {

    @Test
    fun `test code`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
                alwaysValidPathValidator()
            })
        }

        val task = ConstructYoloV3ImageInput("task").apply {
            imageDataInput = configuredCorrectly("var1")
            imageSizeInput = configuredCorrectly("var2")
            sessionInput = configuredCorrectly("var3")
            output = configuredCorrectly("var4")
        }

        task.code() shouldBe "var4 = {var3.get_inputs()[0].name: var1, var3.get_inputs()[1].name: var2}"
    }
}
