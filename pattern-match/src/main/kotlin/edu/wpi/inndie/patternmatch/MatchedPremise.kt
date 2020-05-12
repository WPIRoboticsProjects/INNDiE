package edu.wpi.inndie.patternmatch

import arrow.core.Some

/**
 * Provides methods to get the values of variables for an already-matched [Pattern].
 *
 * @param pattern The [Pattern]. Must already be matched.
 * @param value The value that matched the [pattern].
 */
class MatchedPremise<T : List<E>, E : Any>(
    private val pattern: Pattern<T, E>,
    val value: T
) {

    private val variables by lazy {
        (pattern.match(value) as Some).t
    }

    fun firstMatch(): E = variables[0]
    fun secondMatch(): E = variables[1]
    fun thirdMatch(): E = variables[2]
    fun nthMatch(n: Int): E = variables[n]
}
