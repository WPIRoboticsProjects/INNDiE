package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.testutil.KoinTestFixture
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

internal class LoadStringTaskTest : KoinTestFixture() {

    @Test
    fun `test code gen`() {
        startKoin { }
        LoadStringTask("").apply {
            data = "data"
            output = configuredCorrectly("output")
        }.code().shouldBe("output = \"data\"")
    }
}
