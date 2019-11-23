package edu.wpi.axon.examplemodel

import io.kotlintest.assertions.arrow.either.shouldBeLeft
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.file.shouldExist
import io.kotlintest.shouldBe
import java.io.File
import java.nio.file.Paths
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class GitExampleModelManagerTest {

    private val testExampleModelMetadataUrl =
        "https://raw.githubusercontent.com/wpilibsuite/axon-example-models-testing/master/exampleModels.json"

    @Test
    fun `update cache once`(@TempDir tempDir: File) {
        val manager = getGitExampleModelManagerForTesting(tempDir)
        manager.updateCache().unsafeRunSync()
        manager.cacheDir.shouldExist()
        FileUtils.deleteDirectory(tempDir)
    }

    @Test
    fun `update cache twice`(@TempDir tempDir: File) {
        val manager = getGitExampleModelManagerForTesting(tempDir)

        manager.updateCache().unsafeRunSync()
        manager.cacheDir.shouldExist()

        manager.updateCache().unsafeRunSync()
        manager.cacheDir.shouldExist()
        FileUtils.deleteDirectory(tempDir)
    }

    @Test
    fun `get example models without updating cache first`(@TempDir tempDir: File) {
        val manager = getGitExampleModelManagerForTesting(tempDir)
        manager.getAllExampleModels().attempt().unsafeRunSync().shouldBeLeft()
        FileUtils.deleteDirectory(tempDir)
    }

    @Test
    fun `get example models after updating cache`(@TempDir tempDir: File) {
        val manager = getGitExampleModelManagerForTesting(tempDir)

        manager.updateCache().unsafeRunSync()
        manager.cacheDir.shouldExist()

        manager.getAllExampleModels().unsafeRunSync().shouldContainExactly(
            ExampleModel(
                "Model 1",
                "model1.h5",
                "https://raw.githubusercontent.com/wpilibsuite/axon-example-models-testing/master/models/model1.h5",
                "The first model.",
                listOf(1 to 127)
            ),
            ExampleModel(
                "Model 2",
                "model2.h5",
                "https://raw.githubusercontent.com/wpilibsuite/axon-example-models-testing/master/models/model2.h5",
                "The second model.",
                listOf(1 to 30)
            )
        )
        FileUtils.deleteDirectory(tempDir)
    }

    @Test
    fun `download example models`(@TempDir tempDir: File) {
        val manager = getGitExampleModelManagerForTesting(tempDir)

        manager.updateCache().unsafeRunSync()
        manager.cacheDir.shouldExist()

        manager.getAllExampleModels().unsafeRunSync().forEach { model ->
            manager.download(model).unsafeRunSync().let {
                it.shouldExist()
                it.name.shouldBe(model.fileName)
                it.readText().replace(Regex("\\s"), "").shouldBe(model.fileName)
            }
        }
        FileUtils.deleteDirectory(tempDir)
    }

    @Test
    fun `download twice`(@TempDir tempDir: File) {
        val manager = getGitExampleModelManagerForTesting(tempDir)

        manager.updateCache().unsafeRunSync()
        manager.cacheDir.shouldExist()
        manager.getAllExampleModels().unsafeRunSync().forEach { model ->
            val file = Paths.get(manager.cacheDir.absolutePath, model.fileName).toFile()
            file.createNewFile()
            manager.download(model).unsafeRunSync().let {
                it.shouldExist()
                it.name.shouldBe(model.fileName)
                // Should be empty because we wrote an empty file in the cache before the model was
                // downloaded
                it.readText().shouldBe("")
            }
        }

        FileUtils.deleteDirectory(tempDir)
    }

    @Test
    fun `clone real example models`(@TempDir tempDir: File) {
        val manager = GitExampleModelManager().apply {
            cacheDir = tempDir
        }

        manager.updateCache().unsafeRunSync()
        manager.cacheDir.shouldExist()

        manager.getAllExampleModels().unsafeRunSync().let {
            it.shouldContain(
                ExampleModel(
                    "Inception ResNet V2",
                    "inception_resnet_v2.h5",
                    "https://users.wpi.edu/~rgbenasutti/models/inception_resnet_v2.h5",
                    "A convolutional neural network trained on ImageNet. Better accuracy than any Inception or ResNet versions. Requires a lot of compute power to use.",
                    listOf(1 to 782)
                )
            )

            it.shouldContain(
                ExampleModel(
                    "MobileNet V2",
                    "mobilenetv2_1.00_224.h5",
                    "https://users.wpi.edu/~rgbenasutti/models/mobilenetv2_1.00_224.h5",
                    "A convolutional neural network trained on ImageNet. Acceptable accuracy and moderate compute requirements for mobile robotics.",
                    listOf(1 to 157)
                )
            )
        }
        FileUtils.deleteDirectory(tempDir)
    }

    private fun getGitExampleModelManagerForTesting(tempDir: File) =
        GitExampleModelManager().apply {
            cacheDir = tempDir
            exampleModelMetadataUrl = testExampleModelMetadataUrl
        }
}
