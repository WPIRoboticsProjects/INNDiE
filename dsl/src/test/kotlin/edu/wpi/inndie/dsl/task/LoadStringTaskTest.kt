package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.configuredCorrectly
import edu.wpi.inndie.testutil.KoinTestFixture
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

    @Test
    fun `test escaping the string`() {
        startKoin { }
        LoadStringTask("").apply {
            data = """"quoted" \backslashes\"""
            output = configuredCorrectly("output")
        }.code().shouldBe("""
            output = "\"quoted\" \\backslashes\\"
        """.trimIndent())
    }
}
