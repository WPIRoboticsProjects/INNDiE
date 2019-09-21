package edu.wpi.axon.util

infix fun <E> Iterable<E>.anyIn(other: Iterable<E>) = any { it in other }
