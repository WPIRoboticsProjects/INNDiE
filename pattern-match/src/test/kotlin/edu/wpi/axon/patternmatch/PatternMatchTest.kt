package edu.wpi.axon.patternmatch

import io.kotlintest.assertions.arrow.option.shouldBeNone
import io.kotlintest.assertions.arrow.option.shouldBeSome
import org.junit.jupiter.api.Test

internal class PatternMatchTest {

    @Test
    fun `no patterns returns None`() {
        match<List<String>, String, String>(listOf("a", "b")) {}.shouldBeNone()
    }

    @Test
    fun `no matching patterns returns None`() {
        match<List<String>, String, String>(listOf("a")) {
            pattern("b") returns { firstMatch() }
        }.shouldBeNone()
    }

    @Test
    fun `one matching pattern with a variable returns the value of that variable`() {
        match<List<String>, String, String>(listOf("a", "b")) {
            pattern("a", Variable) returns { firstMatch() }
        }.shouldBeSome("b")
    }

    @Test
    fun `two matching patterns picks the first pattern`() {
        match<List<String>, String, String>(listOf("a", "b", "c")) {
            pattern("a", Variable, Variable) returns { secondMatch() }
            pattern("a", Variable, Variable) returns { firstMatch() }
        }.shouldBeSome("c")
    }
}
