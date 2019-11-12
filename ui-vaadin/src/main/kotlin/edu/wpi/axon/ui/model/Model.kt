package edu.wpi.axon.ui.model

enum class Model(val description: String) {
    UNet("This model is great at detecting horseshoes"),
    MobileNet("This model is great at detecting mobile homes"),
    Resnet("This model is not that great"),
    Mnist("This model is great at detecting numbers")
}
