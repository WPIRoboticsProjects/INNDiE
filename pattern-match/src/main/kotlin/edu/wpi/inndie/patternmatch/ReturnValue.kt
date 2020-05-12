package edu.wpi.inndie.patternmatch

typealias ReturnValue<T, E, R> = MatchedPremise<T, E>.() -> R
