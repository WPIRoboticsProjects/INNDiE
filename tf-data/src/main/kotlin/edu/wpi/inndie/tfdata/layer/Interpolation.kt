package edu.wpi.inndie.tfdata.layer

/**
 * Values for the `interpolation` parameter for sampling-type layers.
 */
enum class Interpolation(val value: String) {
    Nearest("nearest"), Bilinear("bilinear")
}
