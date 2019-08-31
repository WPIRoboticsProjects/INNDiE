package edu.wpi.axon.patternmatch

import arrow.core.None
import arrow.core.Some
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PatternTest {

    @Test
    fun `match with empty list`() {
        val premise = Pattern<List<String>, String>(emptyList())
        assertEquals(Some(emptyList<String>()), premise.match(emptyList()))
    }

    @Test
    fun `mismatch with empty list`() {
        val premise = Pattern<List<String>, String>(emptyList())
        assertEquals(None, premise.match(listOf("val1")))
    }

    @Test
    fun `match with one value`() {
        val premise = Pattern<List<String>, String>(listOf("val1"))
        assertEquals(Some(emptyList<String>()), premise.match(listOf("val1")))
    }

    @Test
    fun `mismatch with one value`() {
        val premise = Pattern<List<String>, String>(listOf("val1"))
        assertEquals(None, premise.match(listOf("val2")))
    }

    @Test
    fun `match with one var`() {
        val premise = Pattern<List<String>, String>(listOf(Variable))
        assertEquals(Some(listOf("var1")), premise.match(listOf("var1")))
    }

    @Test
    fun `match with one value and one var`() {
        val premise = Pattern<List<String>, String>(listOf("val1", Variable))
        assertEquals(Some(listOf("var1")), premise.match(listOf("val1", "var1")))
    }

    @Test
    fun `mismatch on head with one value and one var`() {
        val premise = Pattern<List<String>, String>(listOf("val1", Variable))
        assertEquals(None, premise.match(listOf("val2", "var1")))
    }

    @Test
    fun `mismatch on tail with one value and one var`() {
        val premise = Pattern<List<String>, String>(listOf("val1", Variable))
        assertEquals(None, premise.match(listOf("var1", "val2")))
    }

    @Test
    fun `match with one var and one value`() {
        val premise = Pattern<List<String>, String>(listOf(Variable, "val1"))
        assertEquals(Some(listOf("var1")), premise.match(listOf("var1", "val1")))
    }

    @Test
    fun `match with three values and two vars`() {
        val premise = Pattern<List<String>, String>(
            listOf("val1", Variable, "val2", Variable, "val3")
        )

        assertEquals(
            Some(listOf("var1", "var2")),
            premise.match(listOf("val1", "var1", "val2", "var2", "val3"))
        )
    }

    @Test
    fun `mismatch with three values and two vars`() {
        val premise = Pattern<List<String>, String>(
            listOf("val1", Variable, "val2", Variable, "val3")
        )

        assertEquals(
            None,
            premise.match(listOf("val1", "var1", "val4", "var2", "val3"))
        )
    }
}
