package edu.wpi.axon.ui

internal fun Double.isWholeNumber() = rem(toInt()) == 0.0
