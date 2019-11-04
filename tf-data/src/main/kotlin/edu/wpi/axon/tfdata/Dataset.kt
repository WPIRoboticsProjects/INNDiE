package edu.wpi.axon.tfdata

sealed class Dataset(val name: String) : Comparable<Dataset> {
    object BostonHousing : Dataset("boston_housing")
    object Cifar10 : Dataset("cifar10")
    object Cifar100 : Dataset("cifar100")
    object FashionMnist : Dataset("fashion_mnist")
    object IMDB : Dataset("imdb")
    object Mnist : Dataset("mnist")
    object Reuters : Dataset("reuters")

    override fun compareTo(other: Dataset): Int {
        return COMPARATOR.compare(this, other)
    }

    companion object {
        private val COMPARATOR = Comparator.comparing<Dataset, String> { it.name }
    }
}
