package edu.wpi.axon.patternmatch

import arrow.core.None
import arrow.core.Option
import arrow.core.Some

object Variable

/**
 * Matches a value according to a pattern of [values].
 *
 * @param values The pattern. Variable elements are indicated by [Variable]; everything else will be
 * matched exactly.
 */
class Pattern<T : List<E>, E : Any>(private val values: List<Any>) {

    /**
     * Match a [valueToMatch].
     *
     * @param valueToMatch The value to match.
     * @return [Some] for a match (containing the values of the variables), [None] for no match.
     */
    fun matches(valueToMatch: T): Option<List<E>> {
        if (valueToMatch.size != values.size) {
            return None
        }

        val matchIndices = values.mapIndexedNotNull { index, value ->
            when (value) {
                Variable -> index
                else -> if (valueToMatch[index] != values[index]) {
                    return None
                } else {
                    null
                }
            }
        }

        return Some(matchIndices.map { valueToMatch[it] })
    }
}
