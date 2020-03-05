package edu.wpi.axon.tfdata

import edu.wpi.axon.util.FilePath
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class DatasetTest {

    @ParameterizedTest
    @MethodSource("exampleDatasetSource")
    fun `test example dataset names`(dataset: Dataset.ExampleDataset, expected: String) {
        dataset.name shouldBe expected
    }

    @ParameterizedTest
    @MethodSource("datasetSerializationSource")
    fun `test serialization`(dataset: Dataset.ExampleDataset) {
        Dataset.deserialize(dataset.serialize()).shouldBe(dataset)
    }

    @Test
    fun `test creating custom dataset with spaces in the filename`() {
        // Just testing that this does not throw
        Dataset.Custom(
            FilePath.Local("my dataset.tar"),
            "my dataset"
        )
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        fun exampleDatasetSource() = listOf(
            Arguments.of(Dataset.ExampleDataset.BostonHousing, "boston_housing"),
            Arguments.of(Dataset.ExampleDataset.Cifar10, "cifar10"),
            Arguments.of(Dataset.ExampleDataset.Cifar100, "cifar100"),
            Arguments.of(Dataset.ExampleDataset.FashionMnist, "fashion_mnist"),
            Arguments.of(Dataset.ExampleDataset.IMDB, "imdb"),
            Arguments.of(Dataset.ExampleDataset.Mnist, "mnist"),
            Arguments.of(Dataset.ExampleDataset.Reuters, "reuters")
        )

        @JvmStatic
        @Suppress("unused")
        fun datasetSerializationSource() = listOf(
            Arguments.of(Dataset.ExampleDataset.BostonHousing),
            Arguments.of(Dataset.ExampleDataset.Cifar10),
            Arguments.of(Dataset.ExampleDataset.Cifar100),
            Arguments.of(Dataset.ExampleDataset.FashionMnist),
            Arguments.of(Dataset.ExampleDataset.IMDB),
            Arguments.of(Dataset.ExampleDataset.Mnist),
            Arguments.of(Dataset.ExampleDataset.Reuters)
        )
    }
}
