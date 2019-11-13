package edu.wpi.axon.tfdata

import io.kotlintest.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class DatasetTest {

    @ParameterizedTest
    @MethodSource("exampleDatasetSource")
    fun `test example dataset names`(dataset: Dataset.ExampleDataset, expected: String) {
        dataset.name shouldBe expected
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
    }
}
