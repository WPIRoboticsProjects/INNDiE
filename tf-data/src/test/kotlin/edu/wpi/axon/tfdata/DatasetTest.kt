package edu.wpi.axon.tfdata

import io.kotlintest.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class DatasetTest {

    @ParameterizedTest
    @MethodSource("datasetSource")
    fun `test dataset names`(dataset: Dataset, expected: String) {
        dataset.name shouldBe expected
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        fun datasetSource() = listOf(
            Arguments.of(Dataset.BostonHousing, "boston_housing"),
            Arguments.of(Dataset.Cifar10, "cifar10"),
            Arguments.of(Dataset.Cifar100, "cifar100"),
            Arguments.of(Dataset.FashionMnist, "fashion_mnist"),
            Arguments.of(Dataset.IMDB, "imdb"),
            Arguments.of(Dataset.Mnist, "mnist"),
            Arguments.of(Dataset.Reuters, "reuters")
        )
    }
}
