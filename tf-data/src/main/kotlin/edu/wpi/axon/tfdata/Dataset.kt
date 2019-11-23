package edu.wpi.axon.tfdata

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

@Serializable
sealed class Dataset : Comparable<Dataset> {
    abstract val displayName: String

    @Serializable
    sealed class ExampleDataset(val name: String, override val displayName: String) : Dataset() {
        @Serializable
        object BostonHousing : ExampleDataset("boston_housing", "Boston Housing")

        @Serializable
        object Cifar10 : ExampleDataset("cifar10", "CIFAR-10")

        @Serializable
        object Cifar100 : ExampleDataset("cifar100", "CIFAR-100")

        @Serializable
        object FashionMnist : ExampleDataset("fashion_mnist", "Fashion MNIST")

        @Serializable
        object IMDB : ExampleDataset("imdb", "IMBD")

        @Serializable
        object Mnist : ExampleDataset("mnist", "MNIST")

        @Serializable
        object Reuters : ExampleDataset("reuters", "Reuters")
    }

    @Serializable
    data class Custom(val pathInS3: String, override val displayName: String) : Dataset()

    override fun compareTo(other: Dataset) = COMPARATOR.compare(this, other)

    fun serialize(): String = Json(
        JsonConfiguration.Stable
    ).stringify(serializer(), this)

    companion object {
        private val COMPARATOR = Comparator.comparing<Dataset, String> { it.displayName }

        fun deserialize(data: String) = Json(
            JsonConfiguration.Stable
        ).parse(serializer(), data)
    }
}
