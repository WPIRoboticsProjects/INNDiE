package edu.wpi.axon.tfdata.loss

sealed class Loss {

    object SparseCategoricalCrossentropy : Loss()
}
