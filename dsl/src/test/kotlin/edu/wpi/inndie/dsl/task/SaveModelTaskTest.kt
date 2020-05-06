package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.TaskConfigurationTestFixture
import edu.wpi.inndie.dsl.configuredCorrectly
import edu.wpi.inndie.testutil.KoinTestFixture
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

internal class SaveModelTaskConfigurationTest :
    TaskConfigurationTestFixture<SaveModelTask>(
        { SaveModelTask("").apply { modelPath = "" } },
        listOf(
            SaveModelTask::modelInput
        )
    )

internal class SaveModelTaskTest : KoinTestFixture() {

    @Test
    fun `test code gen`() {
        startKoin { }

        val task = SaveModelTask("").apply {
            modelInput = configuredCorrectly("modelInput")
            modelPath = "test/modelName.h5"
        }

        task.code() shouldBe """
            |try:
            |    os.makedirs(Path("test/modelName.h5").parent)
            |except OSError as err:
            |    if err.errno != errno.EEXIST:
            |        raise
            |
            |modelInput.save("test/modelName.h5")
        """.trimMargin()
    }
}
