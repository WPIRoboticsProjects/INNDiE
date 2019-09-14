package edu.wpi.axon.tfdata

sealed class Dataset(val name: String) {
    object BostonHousing : Dataset("boston_housing")
    object Cifar10 : Dataset("cifar10")
    object Cifar100 : Dataset("cifar100")
    object FashionMnist : Dataset("fashion_mnist")
    object IMDB : Dataset("imdb")
    object Mnist : Dataset("mnist")
    object Reuters : Dataset("reuters")
}
