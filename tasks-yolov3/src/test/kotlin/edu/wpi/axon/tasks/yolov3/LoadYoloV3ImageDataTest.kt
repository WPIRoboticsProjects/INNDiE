package edu.wpi.axon.tasks.yolov3

import edu.wpi.axon.dsl.alwaysValidImportValidator
import edu.wpi.axon.dsl.alwaysValidPathValidator
import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.testutil.KoinTestFixture
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal class LoadYoloV3ImageDataTest : KoinTestFixture() {

    @Test
    fun `test code`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
                alwaysValidPathValidator()
            })
        }

        val task = LoadYoloV3ImageData("task").apply {
            imageInput = configuredCorrectly("var1")
            imageDataOutput = configuredCorrectly("var2")
            imageSizeOutput = configuredCorrectly("var3")
        }

        task.code() shouldBe """
            |var2 = preprocessYoloV3(var1)
            |var3 = np.array([var1.size[1], var1.size[0]], dtype=np.float32).reshape(1, 2)
        """.trimMargin()
    }
}
