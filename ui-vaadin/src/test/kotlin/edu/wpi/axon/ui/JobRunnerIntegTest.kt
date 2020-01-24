package edu.wpi.axon.ui

import arrow.core.Tuple3
import arrow.fx.IO
import arrow.fx.extensions.fx
import defaultFrontendModule
import edu.wpi.axon.aws.S3Manager
import edu.wpi.axon.aws.axonBucketName
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.dsl.defaultBackendModule
import edu.wpi.axon.examplemodel.GitExampleModelManager
import edu.wpi.axon.examplemodel.downloadAndConfigureExampleModel
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.training.testutil.loadModel
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.koin.core.context.startKoin
import org.koin.core.get
import org.koin.core.qualifier.named

internal class JobRunnerIntegTest : KoinTestFixture() {

    @Test
    @Timeout(value = 6L, unit = TimeUnit.MINUTES)
    @Disabled("Needs AWS supervision.")
    fun `test starting job and tracking progress`() {
        startKoin {
            modules(listOf(defaultBackendModule(), defaultFrontendModule()))
        }

        val newModelName = "32_32_1_conv_sequential-trained.h5"
        val (model, path) = loadModel("32_32_1_conv_sequential.h5") {}
        val job = Job(
            "Job 1",
            TrainingScriptProgress.NotStarted,
            path,
            newModelName,
            Dataset.ExampleDataset.FashionMnist,
            Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
            Loss.SparseCategoricalCrossentropy,
            setOf("accuracy"),
            1,
            model,
            false
        )

        val jobRunner = JobRunner()
        jobRunner.startJob(job).flatMap { id ->
            jobRunner.waitForCompleted(id) { println(it) }
        }.unsafeRunSync()
    }

    // TODO: This model doesn't work with the default dataset resizing, we need to configure that
    @Test
    @Timeout(value = 6L, unit = TimeUnit.MINUTES)
    @Disabled("Needs AWS supervision.")
    fun `test starting job with example model`() {
        startKoin {
            modules(listOf(defaultBackendModule(), defaultFrontendModule()))
        }

        val exampleModelManager = GitExampleModelManager()
        val (exampleModel, model, file) = IO.fx {
            exampleModelManager.updateCache().bind()

            val exampleModel = exampleModelManager.getAllExampleModels().bind().first {
                it.fileName == "mobilenetv2_1.00_224.h5"
            }

            val (model, file) = downloadAndConfigureExampleModel(
                exampleModel,
                exampleModelManager
            ).bind()

            Tuple3(exampleModel, model, file)
        }.unsafeRunSync()

        // Need to upload the example model so that EC2 can pull it back down
        S3Manager(get(named(axonBucketName))).uploadUntrainedModel(file)

        val userNewModelName =
            exampleModel.fileName.substringBeforeLast(".") +
                "-trained." +
                exampleModel.fileName.substringAfterLast(".")

        val job = Job(
            "Job 1",
            TrainingScriptProgress.NotStarted,
            file.absolutePath,
            userNewModelName,
            Dataset.ExampleDataset.Mnist,
            Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
            Loss.SparseCategoricalCrossentropy,
            setOf("accuracy"),
            1,
            model,
            false
        )

        val jobRunner = JobRunner()
        jobRunner.startJob(job).flatMap { id ->
            jobRunner.waitForCompleted(id) { println(it) }
        }.unsafeRunSync()
    }
}
