package edu.wpi.axon.dsl

internal infix fun <E> Iterable<E>.anyIn(other: Iterable<E>) = any { it in other }
