package edu.wpi.axon.core.dsl

internal fun <E> List<E>.isAllUnique() = size == toSet().size
