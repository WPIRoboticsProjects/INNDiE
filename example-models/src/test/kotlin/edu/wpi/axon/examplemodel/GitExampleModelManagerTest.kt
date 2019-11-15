package edu.wpi.axon.examplemodel

import io.kotlintest.assertions.arrow.either.shouldBeLeft
import io.kotlintest.assertions.arrow.either.shouldBeRight
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.file.shouldExist
import io.kotlintest.matchers.longs.shouldBeLessThan
import java.io.File
import kotlin.system.measureTimeMillis
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

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
                "models/model1.h5",
                "The first model.",
                listOf(1 to 127)
            ),
            ExampleModel(
                "Model 2",
                "models/model2.h5",
                "The second model.",
                listOf(1 to 30)
            )
        )
    }

    @Test
    @Disabled("Clones the real example models, which is hundreds of MB.")
    fun `clone real example models`(@TempDir tempDir: File) {
        val manager = GitExampleModelManager().apply {
            cacheDir = tempDir
        }

        manager.updateCache().unsafeRunSync()
        manager.exampleModelRepoDir.shouldExist()

        manager.getAllExampleModels().attempt().unsafeRunSync().shouldBeRight()
    }

    private fun getGitExampleModelManagerForTesting(tempDir: File) =
        GitExampleModelManager().apply {
            cacheDir = tempDir
            exampleModelRepo = testExampleModelRepo
        }
}
