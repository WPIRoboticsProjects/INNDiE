package edu.wpi.axon.training

import arrow.core.None
import edu.wpi.axon.plugin.DatasetPlugins
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.inndie.util.FilePath
import io.kotlintest.matchers.booleans.shouldBeFalse
import io.kotlintest.matchers.booleans.shouldBeTrue
import io.mockk.mockk
import java.io.File
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class TrainStateTest {

    @Test
    fun `test all s3 paths`(@TempDir tempDir: File) {
        TrainState(
            FilePath.S3("a"),
            Dataset.Custom(FilePath.S3("d"), "d"),
            Optimizer.Adam(1.0, 1.0, 1.0, 1.0, true),
            Loss.SparseCategoricalCrossentropy,
            emptySet(),
            1,
            mockk<Model>(),
            None,
            false,
            ModelDeploymentTarget.Desktop,
            tempDir.toPath(),
            DatasetPlugins.datasetPassthroughPlugin,
            1
        ).usesAWS.shouldBeTrue()
    }

    @Test
    fun `test s3 paths with an example dataset`(@TempDir tempDir: File) {
        TrainState(
            FilePath.S3("a"),
            Dataset.ExampleDataset.BostonHousing,
            Optimizer.Adam(1.0, 1.0, 1.0, 1.0, true),
            Loss.SparseCategoricalCrossentropy,
            emptySet(),
            1,
            mockk<Model>(),
            None,
            false,
            ModelDeploymentTarget.Desktop,
            tempDir.toPath(),
            DatasetPlugins.datasetPassthroughPlugin,
            1
        ).usesAWS.shouldBeTrue()
    }

    @Test
    fun `test all local paths`(@TempDir tempDir: File) {
        TrainState(
            FilePath.Local("a"),
            Dataset.Custom(FilePath.Local("d"), "d"),
            Optimizer.Adam(1.0, 1.0, 1.0, 1.0, true),
            Loss.SparseCategoricalCrossentropy,
            emptySet(),
            1,
            mockk<Model>(),
            None,
            false,
            ModelDeploymentTarget.Desktop,
            tempDir.toPath(),
            DatasetPlugins.datasetPassthroughPlugin,
            1
        ).usesAWS.shouldBeFalse()
    }

    @Test
    fun `test local paths with an example dataset`(@TempDir tempDir: File) {
        TrainState(
            FilePath.Local("a"),
            Dataset.ExampleDataset.FashionMnist,
            Optimizer.Adam(1.0, 1.0, 1.0, 1.0, true),
            Loss.SparseCategoricalCrossentropy,
            emptySet(),
            1,
            mockk<Model>(),
            None,
            false,
            ModelDeploymentTarget.Desktop,
            tempDir.toPath(),
            DatasetPlugins.datasetPassthroughPlugin,
            1
        ).usesAWS.shouldBeFalse()
    }
}
