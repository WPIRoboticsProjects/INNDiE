package edu.wpi.axon.testutil

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.describe
import com.natpryce.hamkrest.equalTo

fun <T> hasElementWhere(predicate: T.() -> Boolean): Matcher<Collection<T>> =
    object : Matcher.Primitive<Collection<T>>() {

        override fun invoke(actual: Collection<T>): MatchResult =
            if (actual.any(predicate)) {
                MatchResult.Match
            } else {
                MatchResult.Mismatch("was ${describe(actual)}")
            }

        override val description: String
            get() = "contains element where ${describe(predicate)}"

        override val negatedDescription: String
            get() = "does not contain an element where ${describe(predicate)}"
    }

fun <K, V> mapHasElementWhere(predicate: Map.Entry<K, V>.() -> Boolean): Matcher<Map<K, V>> =
    object : Matcher.Primitive<Map<K, V>>() {

        override fun invoke(actual: Map<K, V>): MatchResult =
            if (actual.any(predicate)) {
                MatchResult.Match
            } else {
                MatchResult.Mismatch("was ${describe(actual)}")
            }

        override val description: String
            get() = "contains element where ${describe(predicate)}"

        override val negatedDescription: String
            get() = "does not contain an element where ${describe(predicate)}"
    }

fun isTrue(): Matcher<Boolean?> = equalTo(true)

fun isFalse(): Matcher<Boolean?> = equalTo(false)
