package edu.wpi.axon.training

import arrow.core.None
import edu.wpi.axon.plugin.DatasetPlugins
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.util.FilePath
import io.kotlintest.matchers.booleans.shouldBeFalse
import io.kotlintest.matchers.booleans.shouldBeTrue
import io.kotlintest.shouldThrow
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class TrainStateTest {

    @Test
    fun `test all s3 paths`() {
        TrainState(
            FilePath.S3("a"),
            FilePath.S3("b"),
            Dataset.Custom(FilePath.S3("d"), "d"),
            Optimizer.Adam(1.0, 1.0, 1.0, 1.0, true),
            Loss.SparseCategoricalCrossentropy,
            emptySet(),
            1,
            mockk<Model>(),
            None,
            false,
            ModelDeploymentTarget.Desktop,
            DatasetPlugins.datasetPassthroughPlugin,
            1
        ).usesAWS.shouldBeTrue()
    }

    @Test
    fun `test s3 paths with an example dataset`() {
        TrainState(
            FilePath.S3("a"),
            FilePath.S3("b"),
            Dataset.ExampleDataset.BostonHousing,
            Optimizer.Adam(1.0, 1.0, 1.0, 1.0, true),
            Loss.SparseCategoricalCrossentropy,
            emptySet(),
            1,
            mockk<Model>(),
            None,
            false,
            ModelDeploymentTarget.Desktop,
            DatasetPlugins.datasetPassthroughPlugin,
            1
        ).usesAWS.shouldBeTrue()
    }

    @Test
    fun `test all local paths`() {
        TrainState(
            FilePath.Local("a"),
            FilePath.Local("b"),
            Dataset.Custom(FilePath.Local("d"), "d"),
            Optimizer.Adam(1.0, 1.0, 1.0, 1.0, true),
            Loss.SparseCategoricalCrossentropy,
            emptySet(),
            1,
            mockk<Model>(),
            None,
            false,
            ModelDeploymentTarget.Desktop,
            DatasetPlugins.datasetPassthroughPlugin,
            1
        ).usesAWS.shouldBeFalse()
    }

    @Test
    fun `test local paths with an example dataset`() {
        TrainState(
            FilePath.Local("a"),
            FilePath.Local("b"),
            Dataset.ExampleDataset.FashionMnist,
            Optimizer.Adam(1.0, 1.0, 1.0, 1.0, true),
            Loss.SparseCategoricalCrossentropy,
            emptySet(),
            1,
            mockk<Model>(),
            None,
            false,
            ModelDeploymentTarget.Desktop,
            DatasetPlugins.datasetPassthroughPlugin,
            1
        ).usesAWS.shouldBeFalse()
    }

    @Test
    fun `test s3 and local paths`() {
        shouldThrow<IllegalArgumentException> {
            TrainState(
                FilePath.Local("a"),
                FilePath.S3("b"),
                Dataset.ExampleDataset.FashionMnist,
                Optimizer.Adam(1.0, 1.0, 1.0, 1.0, true),
                Loss.SparseCategoricalCrossentropy,
                emptySet(),
                1,
                mockk<Model>(),
                None,
                false,
                ModelDeploymentTarget.Desktop,
                DatasetPlugins.datasetPassthroughPlugin,
                1
            )
        }
    }
}
