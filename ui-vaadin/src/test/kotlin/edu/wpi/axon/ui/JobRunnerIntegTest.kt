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
import edu.wpi.axon.plugin.DatasetPlugins
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.training.testutil.loadModel
import edu.wpi.axon.util.FilePath
import edu.wpi.axon.util.axonBucketName
import io.kotlintest.matchers.booleans.shouldBeTrue
import io.kotlintest.shouldBe
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir
import org.koin.core.context.startKoin
import org.koin.core.get
import org.koin.core.qualifier.named

internal class JobRunnerIntegTest : KoinTestFixture() {

    @Test
    @Timeout(value = 15L, unit = TimeUnit.MINUTES)
    @Tag("needsTensorFlowSupport")
    fun `test starting local job and tracking progress`(@TempDir tempDir: File) {
        startKoin {
            modules(listOf(defaultBackendModule(), defaultFrontendModule()))
        }

        val db = get<JobDb>()
        val oldModelName = "32_32_1_conv_sequential.h5"
        val newModelName = "$tempDir/32_32_1_conv_sequential-trained.h5"
        val (oldModel, path) = loadModel(oldModelName) {}
        val job = Random.nextJob(
            db,
            name = "Job 1",
            status = TrainingScriptProgress.NotStarted,
            userOldModelPath = FilePath.Local(path),
            userNewModelName = FilePath.Local(newModelName),
            userDataset = Dataset.ExampleDataset.FashionMnist,
            userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
            userLoss = Loss.SparseCategoricalCrossentropy,
            userMetrics = setOf("accuracy"),
            userEpochs = 1,
            userNewModel = oldModel,
            datasetPlugin = Plugin.Unofficial(
                "",
                """
                |def process_dataset(x, y):
                |    newX = tf.cast(x / 255.0, tf.float32)
                |    newY = tf.cast(y / 255.0, tf.float32)
                |    newX = newX[..., tf.newaxis]
                |    newY = newY[..., tf.newaxis]
                |    return (newX, newY)
                """.trimMargin()
            ),
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
        File(newModelName).exists().shouldBeTrue()
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
        val newModelName = "32_32_1_conv_sequential-trained.h5"
        val (oldModel, _) = loadModel(oldModelName) {}
        val job = Random.nextJob(
            db,
            name = "Job 1",
            status = TrainingScriptProgress.NotStarted,
            userOldModelPath = FilePath.S3(oldModelName),
            userNewModelName = FilePath.S3(newModelName),
            userDataset = Dataset.ExampleDataset.FashionMnist,
            userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
            userLoss = Loss.SparseCategoricalCrossentropy,
            userMetrics = setOf("accuracy"),
            userEpochs = 1,
            userNewModel = oldModel,
            datasetPlugin = Plugin.Unofficial(
                "",
                """
                |def process_dataset(x, y):
                |    newX = tf.cast(x / 255.0, tf.float32)
                |    newY = tf.cast(y / 255.0, tf.float32)
                |    newX = newX[..., tf.newaxis]
                |    newY = newY[..., tf.newaxis]
                |    return (newX, newY)
                """.trimMargin()
            ),
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

        val userNewModelName =
            exampleModel.fileName.substringBeforeLast(".") +
                "-trained." +
                exampleModel.fileName.substringAfterLast(".")

        val db = get<JobDb>()
        val job = Random.nextJob(
            db,
            name = "Job 1",
            status = TrainingScriptProgress.NotStarted,
            userOldModelPath = FilePath.S3(file.name),
            userNewModelName = FilePath.S3(userNewModelName),
            userDataset = Dataset.ExampleDataset.Mnist,
            userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
            userLoss = Loss.SparseCategoricalCrossentropy,
            userMetrics = setOf("accuracy"),
            userEpochs = 1,
            userNewModel = model,
            // TODO: Write a dataset plugin to put the dataset in the right format for this model
            datasetPlugin = DatasetPlugins.datasetPassthroughPlugin,
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
