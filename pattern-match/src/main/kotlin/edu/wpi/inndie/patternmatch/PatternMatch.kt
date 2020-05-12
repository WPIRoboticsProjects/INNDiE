package edu.wpi.inndie.patternmatch

import arrow.core.Option
import arrow.core.Some
import arrow.core.firstOrNone

/**
 * Matches a [List] according to patterns.
 *
 * @param T The type of the [List].
 * @param E The type of the element in the [List].
 * @param R The [ReturnValue] return type.
 */
class PatternMatch<T : List<E>, E : Any, R : Any> {

    private val patterns = mutableListOf<Pattern<T, E>>()
    private val returnValues = mutableMapOf<Pattern<T, E>, ReturnValue<T, E, R>>()

    /**
     * Adds a [Pattern].
     *
     * @param values The pattern to match against.
     * @return The new [Pattern].
     */
    fun pattern(vararg values: Any): Pattern<T, E> {
        val pattern = Pattern<T, E>(values.toList())
        patterns += pattern
        return pattern
    }

    /**
     * Adds a [ReturnValue] to a [Pattern].
     *
     * @receiver The [Pattern] to add to.
     * @param returnValue The [ReturnValue] to add.
     */
    infix fun Pattern<T, E>.returns(returnValue: ReturnValue<T, E, R>) {
        returnValues[this] = returnValue
    }

    /**
     * Runs the pattern matching and returns a result.
     *
     * @param value The value to match against.
     * @return [Some] on a match, [None] on no match.
     */
    fun evaluate(value: T): Option<R> = patterns.firstOrNone {
        it.match(value) is Some
    }.map {
        val conclusion = returnValues[it]!!
        MatchedPremise(it, value).conclusion()
    }
}

/**
 * Constructs and evaluates a [PatternMatch].
 */
fun <T : List<E>, E : Any, R : Any> match(
    value: T,
    body: PatternMatch<T, E, R>.(T) -> Unit
): Option<R> {
    val destruct = PatternMatch<T, E, R>()
    destruct.body(value)
    return destruct.evaluate(value)
}
