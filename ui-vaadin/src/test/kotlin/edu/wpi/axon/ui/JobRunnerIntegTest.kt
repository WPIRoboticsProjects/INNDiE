package edu.wpi.axon.ui

import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.dsl.defaultModule
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.training.testutil.loadModel
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ec2.model.InstanceType

internal class JobRunnerIntegTest : KoinTestFixture() {

    @Test
    @Disabled("Needs AWS supervision.")
    fun `test starting job for mobilenet`() {
        startKoin {
            modules(defaultModule())
        }

        val jobRunner = JobRunner(
            "axon-salmon-testbucket2",
            InstanceType.T3_MEDIUM,
            Region.US_EAST_1
        )

        val newModelName = "mobilenetv2_1.00_224-trained.h5"
        val (_, path) = loadModel("mobilenetv2_1.00_224.h5") {}
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
