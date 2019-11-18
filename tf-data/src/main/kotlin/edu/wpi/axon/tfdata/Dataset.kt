package edu.wpi.axon.tfdata

import edu.wpi.axon.util.ObjectSerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule

@Serializable
@Polymorphic
sealed class Dataset : Comparable<Dataset> {
    abstract val displayName: String

    sealed class ExampleDataset(val name: String, override val displayName: String) : Dataset() {
        object BostonHousing : ExampleDataset("boston_housing", "Boston Housing")
        object Cifar10 : ExampleDataset("cifar10", "CIFAR-10")
        object Cifar100 : ExampleDataset("cifar100", "CIFAR-100")
        object FashionMnist : ExampleDataset("fashion_mnist", "Fashion MNIST")
        object IMDB : ExampleDataset("imdb", "IMBD")
        object Mnist : ExampleDataset("mnist", "MNIST")
        object Reuters : ExampleDataset("reuters", "Reuters")
    }

    @Serializable
    data class Custom(val pathInS3: String, override val displayName: String) : Dataset()

    override fun compareTo(other: Dataset): Int {
        return COMPARATOR.compare(this, other)
    }

    companion object {
        private val COMPARATOR = Comparator.comparing<Dataset, String> { it.displayName }
    }
}

val datasetModule = SerializersModule {
    polymorphic(Dataset::class, Dataset.ExampleDataset::class) {
        addSubclass(
            Dataset.ExampleDataset.BostonHousing::class,
            ObjectSerializer(Dataset.ExampleDataset.BostonHousing)
        )
        addSubclass(
            Dataset.ExampleDataset.Cifar10::class,
            ObjectSerializer(Dataset.ExampleDataset.Cifar10)
        )
        addSubclass(
            Dataset.ExampleDataset.Cifar100::class,
            ObjectSerializer(Dataset.ExampleDataset.Cifar100)
        )
        addSubclass(
            Dataset.ExampleDataset.FashionMnist::class,
            ObjectSerializer(Dataset.ExampleDataset.FashionMnist)
        )
        addSubclass(
            Dataset.ExampleDataset.IMDB::class,
            ObjectSerializer(Dataset.ExampleDataset.IMDB)
        )
        addSubclass(
            Dataset.ExampleDataset.Mnist::class,
            ObjectSerializer(Dataset.ExampleDataset.Mnist)
        )
        addSubclass(
            Dataset.ExampleDataset.Reuters::class,
            ObjectSerializer(Dataset.ExampleDataset.Reuters)
        )
        addSubclass(
            Dataset.Custom::class,
            Dataset.Custom.serializer()
        )
    }
}
