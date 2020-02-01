package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.alwaysValidImportValidator
import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.dsl.configuredIncorrectly
import edu.wpi.axon.testutil.KoinTestFixture
import io.kotlintest.matchers.booleans.shouldBeFalse
import io.kotlintest.matchers.booleans.shouldBeTrue
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal class RunPluginTaskTest : KoinTestFixture() {

    @Test
    fun `test code gen`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = RunPluginTask("").apply {
            functionName = "name"
            functionDefinition = "body"
            functionInputs = listOf(configuredCorrectly("in1"), configuredCorrectly("in2"))
            functionOutputs = listOf(configuredCorrectly("out1"), configuredCorrectly("out2"))
        }
        task.isConfiguredCorrectly().shouldBeTrue()
        task.code().shouldBe(
            """|
            |
            |
            |body
            |
            |
            |(out1, out2) = name(in1, in2)
        """.trimMargin()
        )
    }

    @Test
    fun `test config with an invalid input`() {
        startKoin { }

        val task = RunPluginTask("").apply {
            functionName = "name"
            functionDefinition = "body"
            functionInputs = listOf(configuredCorrectly("in1"), configuredIncorrectly("in2"))
            functionOutputs = listOf(configuredCorrectly("out1"), configuredCorrectly("out2"))
        }
        task.isConfiguredCorrectly().shouldBeFalse()
    }

    @Test
    fun `test config with an invalid output`() {
        startKoin { }

        val task = RunPluginTask("").apply {
            functionName = "name"
            functionDefinition = "body"
            functionInputs = listOf(configuredCorrectly("in1"), configuredCorrectly("in2"))
            functionOutputs = listOf(configuredCorrectly("out1"), configuredIncorrectly("out2"))
        }
        task.isConfiguredCorrectly().shouldBeFalse()
    }
}