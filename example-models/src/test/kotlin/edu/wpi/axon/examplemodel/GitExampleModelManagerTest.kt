package edu.wpi.axon.examplemodel

import io.kotlintest.assertions.arrow.either.shouldBeLeft
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.file.shouldExist
import io.kotlintest.matchers.longs.shouldBeLessThan
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.system.measureTimeMillis

internal class GitExampleModelManagerTest {

    private val testExampleModelRepo =
        "https://github.com/wpilibsuite/axon-example-models-testing.git"

    @Test
    fun `update cache once`(@TempDir tempDir: File) {
        val manager = getGitExampleModelManagerForTesting(tempDir)
        manager.updateCache().unsafeRunSync()
        manager.exampleModelRepoDir.shouldExist()
    }

    @Test
    fun `update cache twice`(@TempDir tempDir: File) {
        val manager = getGitExampleModelManagerForTesting(tempDir)

        val timeForFirstUpdate = measureTimeMillis {
            manager.updateCache().unsafeRunSync()
        }
        manager.exampleModelRepoDir.shouldExist()

        val timeForSecondUpdate = measureTimeMillis {
            manager.updateCache().unsafeRunSync()
        }
        manager.exampleModelRepoDir.shouldExist()

        timeForSecondUpdate.shouldBeLessThan(timeForFirstUpdate)
    }

    @Test
    fun `get example models without updating cache first`(@TempDir tempDir: File) {
        val manager = getGitExampleModelManagerForTesting(tempDir)
        manager.getAllExampleModels().attempt().unsafeRunSync().shouldBeLeft()
    }

    @Test
    fun `get example models after updating cache`(@TempDir tempDir: File) {
        val manager = getGitExampleModelManagerForTesting(tempDir)

        manager.updateCache().unsafeRunSync()
        manager.exampleModelRepoDir.shouldExist()

        manager.getAllExampleModels().unsafeRunSync().shouldContainExactly(
            ExampleModel(
                "Model 1",
                "https://github.com/wpilibsuite/axon-example-models-testing/tree/master/models/model1.h5",
                "The first model.",
                listOf(1 to 127)
            ),
            ExampleModel(
                "Model 2",
                "https://github.com/wpilibsuite/axon-example-models-testing/tree/master/models/model2.h5",
                "The second model.",
                listOf(1 to 30)
            )
        )
    }

    @Test
    fun `clone real example models`(@TempDir tempDir: File) {
        val manager = GitExampleModelManager().apply {
            cacheDir = tempDir
        }

        manager.updateCache().unsafeRunSync()
        manager.exampleModelRepoDir.shouldExist()

        manager.getAllExampleModels().unsafeRunSync().let {
            it.shouldContain(
                ExampleModel(
                    "Inception ResNet V2",
                    "https://users.wpi.edu/~rgbenasutti/models/inception_resnet_v2.h5",
                    "A convolutional neural network trained on ImageNet. Better accuracy than any Inception or ResNet versions. Requires a lot of compute power to use.",
                    listOf(1 to 782)
                )
            )

            it.shouldContain(
                ExampleModel(
                    "MobileNet V2",
                    "https://users.wpi.edu/~rgbenasutti/models/mobilenetv2_1.00_224.h5",
                    "A convolutional neural network trained on ImageNet. Acceptable accuracy and moderate compute requirements for mobile robotics.",
                    listOf(1 to 157)
                )
            )
        }
    }

    private fun getGitExampleModelManagerForTesting(tempDir: File) =
        GitExampleModelManager().apply {
            cacheDir = tempDir
            exampleModelRepo = testExampleModelRepo
        }
}
