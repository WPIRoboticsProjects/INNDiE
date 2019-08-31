package edu.wpi.axon.patternmatch

import arrow.core.None
import arrow.core.Some
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PatternMatchTest {

    @Test
    fun `no patterns`() {
        val result = match<List<String>, String, String>(listOf("a", "b")) {}
        assertEquals(None, result)
    }

    @Test
    fun `one matching pattern`() {
        val result = match<List<String>, String, String>(listOf("a", "b")) {
            pattern("a", Variable) returns { firstMatch() }
        }

        assertEquals(Some("b"), result)
    }

    @Test
    fun `two matching patterns picks the first one`() {
        val result = match<List<String>, String, String>(listOf("a", "b", "c")) {
            pattern("a", Variable, Variable) returns { secondMatch() }
            pattern("a", Variable, Variable) returns { firstMatch() }
        }

        assertEquals(Some("c"), result)
    }
}
