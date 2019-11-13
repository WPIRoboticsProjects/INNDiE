package edu.wpi.axon.tfdata

import edu.wpi.axon.util.ObjectSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule

sealed class Dataset {

    sealed class ExampleDataset(val name: String) : Dataset() {
        object BostonHousing : ExampleDataset("boston_housing")
        object Cifar10 : ExampleDataset("cifar10")
        object Cifar100 : ExampleDataset("cifar100")
        object FashionMnist : ExampleDataset("fashion_mnist")
        object IMDB : ExampleDataset("imdb")
        object Mnist : ExampleDataset("mnist")
        object Reuters : ExampleDataset("reuters")
    }

    @Serializable
    data class Custom(val pathInS3: String) : Dataset()
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
