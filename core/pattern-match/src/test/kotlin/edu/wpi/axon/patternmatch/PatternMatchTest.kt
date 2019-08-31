package edu.wpi.axon.patternmatch

import io.kotlintest.assertions.arrow.option.shouldBeNone
import io.kotlintest.assertions.arrow.option.shouldBeSome
import io.kotlintest.specs.StringSpec

internal class PatternMatchTest : StringSpec({
    "no patterns returns None" {
        match<List<String>, String, String>(listOf("a", "b")) {}.shouldBeNone()
    }

    "one matching pattern with a variable returns the value of that variable" {
        match<List<String>, String, String>(listOf("a", "b")) {
            pattern("a", Variable) returns { firstMatch() }
        }.shouldBeSome("b")
    }

    "two matching patterns picks the first pattern" {
        match<List<String>, String, String>(listOf("a", "b", "c")) {
            pattern("a", Variable, Variable) returns { secondMatch() }
            pattern("a", Variable, Variable) returns { firstMatch() }
        }.shouldBeSome("c")
    }
})
