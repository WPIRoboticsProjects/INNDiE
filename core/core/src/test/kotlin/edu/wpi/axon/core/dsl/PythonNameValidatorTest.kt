package edu.wpi.axon.core.dsl

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class PythonNameValidatorTest {

    @ParameterizedTest
    @MethodSource("pythonNameSource")
    fun testPythonNames(name: String, isValid: Boolean) {
        assertThat(isValidPythonIdentifier(name), equalTo(isValid))
    }

    companion object {

        @Suppress("unused")
        @JvmStatic
        fun pythonNameSource() = listOf(
            Arguments.of("a", true),
            Arguments.of("A", true),
            Arguments.of("a_", true),
            Arguments.of("_a", true),
            Arguments.of("a-", false),
            Arguments.of("a-b", false),
            Arguments.of("-a", false),
            Arguments.of("a0", true),
            Arguments.of("_", false),
            Arguments.of("0", false),
            Arguments.of("", false),
            Arguments.of("*", false),
            Arguments.of("<", false),
            Arguments.of("!", false)
        )
    }
}
