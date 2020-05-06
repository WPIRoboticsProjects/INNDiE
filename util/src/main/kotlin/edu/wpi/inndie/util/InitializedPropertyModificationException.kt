package edu.wpi.inndie.util

/**
 * Thrown when a property that is already initialized is modified.
 */
class InitializedPropertyModificationException : RuntimeException {

    constructor()

    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)

    constructor(cause: Throwable?) : super(cause)
}
