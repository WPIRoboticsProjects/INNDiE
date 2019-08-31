package edu.wpi.axon.patternmatch

typealias ReturnValue<T, E, R> = MatchedPremise<T, E>.() -> R
