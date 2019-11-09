package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture
import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.testutil.KoinTestFixture
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

internal class SaveModelTaskConfigurationTest :
    TaskConfigurationTestFixture<SaveModelTask>(
        { SaveModelTask("").apply { modelFileName = "" } },
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
            modelFileName = "modelName.h5"
        }

        task.code() shouldBe """
            |modelInput.save("modelName.h5")
        """.trimMargin()
    }
}
