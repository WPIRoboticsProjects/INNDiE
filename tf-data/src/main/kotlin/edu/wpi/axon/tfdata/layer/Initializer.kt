package edu.wpi.axon.tfdata.layer

/**
 * Methods to initialize weights.
 *
 * TODO: https://github.com/wpilibsuite/Axon/issues/90
 */
sealed class Initializer {

    object Zeros : Initializer()

    object Ones : Initializer()
}
