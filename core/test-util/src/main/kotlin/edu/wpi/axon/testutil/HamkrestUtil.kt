package edu.wpi.axon.testutil

import arrow.core.Either
import arrow.data.Invalid
import arrow.data.Valid
import arrow.data.Validated
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

fun <A, B> isLeft(): Matcher<Either<A, B>?> = object : Matcher<Either<A, B>?> {

    override val description = "is Left"

    override fun invoke(actual: Either<A, B>?) = when (actual) {
        is Either.Left -> MatchResult.Match
        is Either.Right -> MatchResult.Mismatch("was Right")
        null -> MatchResult.Mismatch("was null")
    }
}

fun <A, B> isRight(): Matcher<Either<A, B>?> = object : Matcher<Either<A, B>?> {

    override val description = "is Right"

    override fun invoke(actual: Either<A, B>?) = when (actual) {
        is Either.Left -> MatchResult.Match
        is Either.Right -> MatchResult.Mismatch("was Left")
        null -> MatchResult.Mismatch("was null")
    }
}

fun <A, B> isValid(): Matcher<Validated<A, B>?> = object : Matcher<Validated<A, B>?> {

    override val description = "is Valid"

    override fun invoke(actual: Validated<A, B>?) = when (actual) {
        is Invalid -> MatchResult.Mismatch("was Invalid")
        is Valid -> MatchResult.Match
        null -> MatchResult.Mismatch("was null")
    }
}

fun <A, B> isInvalid(): Matcher<Validated<A, B>?> = object : Matcher<Validated<A, B>?> {

    override val description = "is Invalid"

    override fun invoke(actual: Validated<A, B>?) = when (actual) {
        is Invalid -> MatchResult.Match
        is Valid -> MatchResult.Mismatch("was Valid")
        null -> MatchResult.Mismatch("was null")
    }
}
