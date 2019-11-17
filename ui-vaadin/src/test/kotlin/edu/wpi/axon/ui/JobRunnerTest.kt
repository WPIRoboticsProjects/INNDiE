package edu.wpi.axon.ui

import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.tflayerloader.DefaultLayersToGraph
import edu.wpi.axon.tflayerloader.LoadLayersFromHDF5
import io.kotlintest.assertions.arrow.either.shouldBeRight
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ec2.model.InstanceType
import java.io.File
import java.nio.file.Paths

internal class JobRunnerTest {

    @Test
    @Disabled("Needs EC2 supervision.")
    fun `test starting job for mobilenet`() {
        val jobRunner = JobRunner(
            "axon-salmon-testbucket2",
            InstanceType.T3_MEDIUM,
            Region.US_EAST_1
        )

        val newModelName = "mobilenetv2_1.00_224-trained.h5"
        val (_, path) = loadModel("mobilenetv2_1.00_224.h5")
        val job = Job(
            "Job 1",
            TrainingScriptProgress.NotStarted,
            path,
            newModelName,
            Dataset.ExampleDataset.Mnist,
            Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
            Loss.SparseCategoricalCrossentropy,
            setOf("accuracy"),
            1,
            false
        )

        jobRunner.startJob(job)
    }
}

// TODO: Put this into a test util project so it's not copy-pasted
internal fun loadModel(modelName: String, stub: () -> Unit = {}): Pair<Model, String> {
    val localModelPath = Paths.get(stub::class.java.getResource(modelName).toURI()).toString()
    val layers = LoadLayersFromHDF5(DefaultLayersToGraph())
        .load(File(localModelPath))
    val model = layers.attempt().unsafeRunSync()
    model.shouldBeRight()
    return model.b as Model to localModelPath
}
