package edu.wpi.axon.ui

import arrow.core.Tuple3
import arrow.fx.IO
import arrow.fx.extensions.fx
import edu.wpi.axon.aws.S3Manager
import edu.wpi.axon.db.JobDb
import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.db.data.nextJob
import edu.wpi.axon.dsl.defaultBackendModule
import edu.wpi.axon.examplemodel.GitExampleModelManager
import edu.wpi.axon.examplemodel.downloadAndConfigureExampleModel
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.training.ModelDeploymentTarget
import edu.wpi.axon.training.testutil.loadModel
import edu.wpi.axon.util.FilePath
import edu.wpi.axon.util.axonBucketName
import edu.wpi.axon.util.getOutputModelName
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.shouldBe
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.koin.core.context.startKoin
import org.koin.core.get
import org.koin.core.qualifier.named

internal class JobRunnerIntegTest : KoinTestFixture() {

    @BeforeEach
    fun beforeEach() {
        localScriptRunnerCache.toFile().deleteRecursively()
    }

    @Test
    @Timeout(value = 15L, unit = TimeUnit.MINUTES)
    @Tag("needsTensorFlowSupport")
    fun `test starting local job and tracking progress`() {
        startKoin {
            modules(listOf(defaultBackendModule(), defaultFrontendModule()))
        }

        val db = get<JobDb>()
        val oldModelName = "32_32_1_conv_sequential.h5"
        val (oldModel, path) = loadModel(oldModelName) {}
        val job = Random.nextJob(
            db,
            name = "Job 1",
            status = TrainingScriptProgress.NotStarted,
            userOldModelPath = FilePath.Local(path),
            userDataset = Dataset.ExampleDataset.FashionMnist,
            userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
            userLoss = Loss.SparseCategoricalCrossentropy,
            userMetrics = setOf("accuracy"),
            userEpochs = 1,
            userNewModel = oldModel,
            target = ModelDeploymentTarget.Desktop,
            generateDebugComments = false
        )

        val jobRunner = JobRunner()
        jobRunner.startJob(job)
        var status = job.status
        runBlocking {
            jobRunner.waitForFinish(job.id) {
                println(it)
                status = it
            }
        }
        status.shouldBe(TrainingScriptProgress.Completed)
        jobRunner.listResults(job.id).shouldContain(getOutputModelName(oldModelName))
    }

    @Test
    @Timeout(value = 15L, unit = TimeUnit.MINUTES)
    @Tag("needsTensorFlowSupport")
    fun `test starting local job and tracking progress targeting the coral`() {
        startKoin {
            modules(listOf(defaultBackendModule(), defaultFrontendModule()))
        }

        val db = get<JobDb>()
        val oldModelName = "32_32_1_conv_sequential.h5"
        val (oldModel, path) = loadModel(oldModelName) {}
        val job = Random.nextJob(
            db,
            name = "Job 1",
            status = TrainingScriptProgress.NotStarted,
            userOldModelPath = FilePath.Local(path),
            userDataset = Dataset.ExampleDataset.FashionMnist,
            userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
            userLoss = Loss.SparseCategoricalCrossentropy,
            userMetrics = setOf("accuracy"),
            userEpochs = 1,
            userNewModel = oldModel,
            target = ModelDeploymentTarget.Coral(0.001),
            generateDebugComments = false
        )

        val jobRunner = JobRunner()
        jobRunner.startJob(job)
        var status = job.status
        runBlocking {
            jobRunner.waitForFinish(job.id) {
                println(it)
                status = it
            }
        }
        status.shouldBe(TrainingScriptProgress.Completed)
        jobRunner.listResults(job.id).shouldContainAll(
            "32_32_1_conv_sequential-trained.h5",
            "32_32_1_conv_sequential-trained.tflite",
            "32_32_1_conv_sequential-trained_edgetpu.tflite",
            "32_32_1_conv_sequential-trained_edgetpu.log"
        )
    }

    @Test
    @Timeout(value = 15L, unit = TimeUnit.MINUTES)
    @Disabled("Needs AWS supervision.")
    fun `test starting AWS job and tracking progress`() {
        startKoin {
            modules(listOf(defaultBackendModule(), defaultFrontendModule()))
        }

        val db = get<JobDb>()
        val oldModelName = "32_32_1_conv_sequential.h5"
        val (oldModel, _) = loadModel(oldModelName) {}
        val job = Random.nextJob(
            db,
            name = "Job 1",
            status = TrainingScriptProgress.NotStarted,
            userOldModelPath = FilePath.S3(oldModelName),
            userDataset = Dataset.ExampleDataset.FashionMnist,
            userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
            userLoss = Loss.SparseCategoricalCrossentropy,
            userMetrics = setOf("accuracy"),
            userEpochs = 1,
            userNewModel = oldModel,
            target = ModelDeploymentTarget.Desktop,
            generateDebugComments = false
        )

        val jobRunner = JobRunner()
        jobRunner.startJob(job)
        var status = job.status
        runBlocking {
            jobRunner.waitForFinish(job.id) {
                println(it)
                status = it
            }
        }
        status.shouldBe(TrainingScriptProgress.Completed)
        jobRunner.listResults(job.id).shouldContain(getOutputModelName(oldModelName))
    }

    // TODO: This model doesn't work with the default dataset resizing, we need to configure that
    @Test
    @Timeout(value = 15L, unit = TimeUnit.MINUTES)
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

        val db = get<JobDb>()
        val job = Random.nextJob(
            db,
            name = "Job 1",
            status = TrainingScriptProgress.NotStarted,
            userOldModelPath = FilePath.S3(file.name),
            userDataset = Dataset.ExampleDataset.Mnist,
            userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
            userLoss = Loss.SparseCategoricalCrossentropy,
            userMetrics = setOf("accuracy"),
            userEpochs = 1,
            userNewModel = model,
            target = ModelDeploymentTarget.Desktop,
            generateDebugComments = false
        )

        val jobRunner = JobRunner()
        jobRunner.startJob(job)
        var status = job.status
        runBlocking {
            jobRunner.waitForFinish(job.id) {
                println(it)
                status = it
            }
        }
        status.shouldBe(TrainingScriptProgress.Completed)
    }
}
