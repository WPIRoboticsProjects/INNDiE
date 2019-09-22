package edu.wpi.axon.dsl

import edu.wpi.axon.dsl.variable.Variable
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.MockKException
import org.junit.jupiter.api.Test

internal class MockUtilTest {

    @Test
    fun `test mock variable name validator`() {
        val validator = mockVariableNameValidator("a" to true, "b" to false)
        validator.isValidVariableName("a").shouldBeTrue()
        validator.isValidVariableName("b").shouldBeFalse()
    }

    @Test
    fun `test mock path validator`() {
        val validator = mockPathValidator("a" to true, "b" to false)
        validator.isValidPathName("a").shouldBeTrue()
        validator.isValidPathName("b").shouldBeFalse()
    }

    @Test
    fun `test variable configured correctly`() {
        val variable = configuredCorrectly<Variable>("a")
        variable.name shouldBe "a"
        variable.isConfiguredCorrectly().shouldBeTrue()
    }

    @Test
    fun `test variable with no name configured correctly`() {
        val variable = configuredCorrectly<Variable>()
        shouldThrow<MockKException> { variable.name }
        variable.isConfiguredCorrectly().shouldBeTrue()
    }

    @Test
    fun `test non-variable configured correctly`() {
        val configurable = configuredCorrectly<Configurable>()
        configurable.isConfiguredCorrectly().shouldBeTrue()
    }

    @Test
    fun `test variable configured incorrectly`() {
        val variable = configuredIncorrectly<Variable>("a")
        variable.name shouldBe "a"
        variable.isConfiguredCorrectly().shouldBeFalse()
    }

    @Test
    fun `test variable with no name configured incorrectly`() {
        val variable = configuredIncorrectly<Variable>()
        shouldThrow<MockKException> { variable.name }
        variable.isConfiguredCorrectly().shouldBeFalse()
    }

    @Test
    fun `test non-variable configured incorrectly`() {
        val configurable = configuredIncorrectly<Configurable>()
        configurable.isConfiguredCorrectly().shouldBeFalse()
    }
}
