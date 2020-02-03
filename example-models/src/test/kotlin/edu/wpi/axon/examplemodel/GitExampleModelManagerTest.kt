package edu.wpi.axon.examplemodel

import arrow.core.None
import arrow.core.Some
import edu.wpi.axon.tfdata.SerializableOptionB
import io.kotlintest.assertions.arrow.either.shouldBeLeft
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.file.shouldExist
import io.kotlintest.shouldBe
import java.io.File
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
                mapOf(
                    "input_1" to None,
                    "Conv1_pad" to Some(true)
                ).mapValues { SerializableOptionB.fromOption(it.value) }
            ),
            ExampleModel(
                "Model 2",
                "model2.h5",
                "https://raw.githubusercontent.com/wpilibsuite/axon-example-models-testing/master/models/model2.h5",
                "The second model.",
                mapOf(
                    "input_1" to None,
                    "Conv1_pad" to Some(true)
                ).mapValues { SerializableOptionB.fromOption(it.value) }
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
            val file = manager.cacheDir.toPath().resolve(model.fileName).toFile()
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
                    "MobileNet V2",
                    "mobilenetv2_1.00_224.h5",
                    "https://userweb.wpi.edu/~rgbenasutti/models/mobilenetv2_1.00_224.h5",
                    "A convolutional neural network trained on ImageNet. Acceptable accuracy and moderate compute requirements for mobile robotics.",
                    mapOf(
                        "input_1" to None,
                        "Conv1_pad" to Some(false),
                        "Conv1" to Some(false),
                        "bn_Conv1" to Some(false),
                        "Conv1_relu" to Some(false),
                        "expanded_conv_depthwise" to Some(false),
                        "expanded_conv_depthwise_BN" to Some(false),
                        "expanded_conv_depthwise_relu" to Some(false),
                        "expanded_conv_project" to Some(false),
                        "expanded_conv_project_BN" to Some(false),
                        "block_1_expand" to Some(false),
                        "block_1_expand_BN" to Some(false),
                        "block_1_expand_relu" to Some(false),
                        "block_1_pad" to Some(false),
                        "block_1_depthwise" to Some(false),
                        "block_1_depthwise_BN" to Some(false),
                        "block_1_depthwise_relu" to Some(false),
                        "block_1_project" to Some(false),
                        "block_1_project_BN" to Some(false),
                        "block_2_expand" to Some(false),
                        "block_2_expand_BN" to Some(false),
                        "block_2_expand_relu" to Some(false),
                        "block_2_depthwise" to Some(false),
                        "block_2_depthwise_BN" to Some(false),
                        "block_2_depthwise_relu" to Some(false),
                        "block_2_project" to Some(false),
                        "block_2_project_BN" to Some(false),
                        "block_2_add" to Some(false),
                        "block_3_expand" to Some(false),
                        "block_3_expand_BN" to Some(false),
                        "block_3_expand_relu" to Some(false),
                        "block_3_pad" to Some(false),
                        "block_3_depthwise" to Some(false),
                        "block_3_depthwise_BN" to Some(false),
                        "block_3_depthwise_relu" to Some(false),
                        "block_3_project" to Some(false),
                        "block_3_project_BN" to Some(false),
                        "block_4_expand" to Some(false),
                        "block_4_expand_BN" to Some(false),
                        "block_4_expand_relu" to Some(false),
                        "block_4_depthwise" to Some(false),
                        "block_4_depthwise_BN" to Some(false),
                        "block_4_depthwise_relu" to Some(false),
                        "block_4_project" to Some(false),
                        "block_4_project_BN" to Some(false),
                        "block_4_add" to Some(false),
                        "block_5_expand" to Some(false),
                        "block_5_expand_BN" to Some(false),
                        "block_5_expand_relu" to Some(false),
                        "block_5_depthwise" to Some(false),
                        "block_5_depthwise_BN" to Some(false),
                        "block_5_depthwise_relu" to Some(false),
                        "block_5_project" to Some(false),
                        "block_5_project_BN" to Some(false),
                        "block_5_add" to Some(false),
                        "block_6_expand" to Some(false),
                        "block_6_expand_BN" to Some(false),
                        "block_6_expand_relu" to Some(false),
                        "block_6_pad" to Some(false),
                        "block_6_depthwise" to Some(false),
                        "block_6_depthwise_BN" to Some(false),
                        "block_6_depthwise_relu" to Some(false),
                        "block_6_project" to Some(false),
                        "block_6_project_BN" to Some(false),
                        "block_7_expand" to Some(false),
                        "block_7_expand_BN" to Some(false),
                        "block_7_expand_relu" to Some(false),
                        "block_7_depthwise" to Some(false),
                        "block_7_depthwise_BN" to Some(false),
                        "block_7_depthwise_relu" to Some(false),
                        "block_7_project" to Some(false),
                        "block_7_project_BN" to Some(false),
                        "block_7_add" to Some(false),
                        "block_8_expand" to Some(false),
                        "block_8_expand_BN" to Some(false),
                        "block_8_expand_relu" to Some(false),
                        "block_8_depthwise" to Some(false),
                        "block_8_depthwise_BN" to Some(false),
                        "block_8_depthwise_relu" to Some(false),
                        "block_8_project" to Some(false),
                        "block_8_project_BN" to Some(false),
                        "block_8_add" to Some(false),
                        "block_9_expand" to Some(false),
                        "block_9_expand_BN" to Some(false),
                        "block_9_expand_relu" to Some(false),
                        "block_9_depthwise" to Some(false),
                        "block_9_depthwise_BN" to Some(false),
                        "block_9_depthwise_relu" to Some(false),
                        "block_9_project" to Some(false),
                        "block_9_project_BN" to Some(false),
                        "block_9_add" to Some(false),
                        "block_10_expand" to Some(false),
                        "block_10_expand_BN" to Some(false),
                        "block_10_expand_relu" to Some(false),
                        "block_10_depthwise" to Some(false),
                        "block_10_depthwise_BN" to Some(false),
                        "block_10_depthwise_relu" to Some(false),
                        "block_10_project" to Some(false),
                        "block_10_project_BN" to Some(false),
                        "block_11_expand" to Some(false),
                        "block_11_expand_BN" to Some(false),
                        "block_11_expand_relu" to Some(false),
                        "block_11_depthwise" to Some(false),
                        "block_11_depthwise_BN" to Some(false),
                        "block_11_depthwise_relu" to Some(false),
                        "block_11_project" to Some(false),
                        "block_11_project_BN" to Some(false),
                        "block_11_add" to Some(false),
                        "block_12_expand" to Some(false),
                        "block_12_expand_BN" to Some(false),
                        "block_12_expand_relu" to Some(false),
                        "block_12_depthwise" to Some(false),
                        "block_12_depthwise_BN" to Some(false),
                        "block_12_depthwise_relu" to Some(false),
                        "block_12_project" to Some(false),
                        "block_12_project_BN" to Some(false),
                        "block_12_add" to Some(false),
                        "block_13_expand" to Some(false),
                        "block_13_expand_BN" to Some(false),
                        "block_13_expand_relu" to Some(false),
                        "block_13_pad" to Some(false),
                        "block_13_depthwise" to Some(false),
                        "block_13_depthwise_BN" to Some(false),
                        "block_13_depthwise_relu" to Some(false),
                        "block_13_project" to Some(false),
                        "block_13_project_BN" to Some(false),
                        "block_14_expand" to Some(false),
                        "block_14_expand_BN" to Some(false),
                        "block_14_expand_relu" to Some(false),
                        "block_14_depthwise" to Some(false),
                        "block_14_depthwise_BN" to Some(false),
                        "block_14_depthwise_relu" to Some(false),
                        "block_14_project" to Some(false),
                        "block_14_project_BN" to Some(false),
                        "block_14_add" to Some(false),
                        "block_15_expand" to Some(false),
                        "block_15_expand_BN" to Some(false),
                        "block_15_expand_relu" to Some(false),
                        "block_15_depthwise" to Some(false),
                        "block_15_depthwise_BN" to Some(false),
                        "block_15_depthwise_relu" to Some(false),
                        "block_15_project" to Some(false),
                        "block_15_project_BN" to Some(false),
                        "block_15_add" to Some(false),
                        "block_16_expand" to Some(false),
                        "block_16_expand_BN" to Some(false),
                        "block_16_expand_relu" to Some(false),
                        "block_16_depthwise" to Some(false),
                        "block_16_depthwise_BN" to Some(false),
                        "block_16_depthwise_relu" to Some(false),
                        "block_16_project" to Some(false),
                        "block_16_project_BN" to Some(false),
                        "Conv_1" to Some(false),
                        "Conv_1_bn" to Some(false),
                        "out_relu" to Some(false),
                        "global_average_pooling2d" to Some(false),
                        "Logits" to Some(true)
                    ).mapValues { SerializableOptionB.fromOption(it.value) }
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
