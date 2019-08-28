package edu.wpi.axon.dsl

internal fun <E> List<E>.isAllUnique() = size == toSet().size

internal infix fun <E> Iterable<E>.anyIn(other: Iterable<E>) = any { it in other }
