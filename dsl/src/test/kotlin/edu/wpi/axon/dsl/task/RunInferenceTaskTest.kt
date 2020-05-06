package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.inndie.testutil.KoinTestFixture
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

internal class RunInferenceTaskTest : KoinTestFixture() {

    @Test
    fun `test code gen`() {
        startKoin { }
        RunInferenceTask("").apply {
            model = configuredCorrectly("model")
            input = configuredCorrectly("input")
            steps = configuredCorrectly("steps")
            output = configuredCorrectly("output")
        }.code().shouldBe(
            """
            |output = model.predict(input, steps=steps)
            """.trimMargin()
        )
    }
}
