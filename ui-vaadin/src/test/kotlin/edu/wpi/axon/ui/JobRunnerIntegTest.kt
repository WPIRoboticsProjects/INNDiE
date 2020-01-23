package edu.wpi.axon.ui

import arrow.core.Tuple3
import arrow.fx.IO
import arrow.fx.extensions.fx
import edu.wpi.axon.aws.EC2TrainingScriptRunner
import edu.wpi.axon.aws.TrainingScriptRunner
import edu.wpi.axon.aws.axonBucketName
import edu.wpi.axon.aws.findAxonS3Bucket
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.dsl.defaultModule
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
import org.koin.core.qualifier.named
import org.koin.dsl.module
import software.amazon.awssdk.services.ec2.model.InstanceType

internal class JobRunnerIntegTest : KoinTestFixture() {

    private val bucketName = findAxonS3Bucket()!!

    @Test
    @Timeout(value = 6L, unit = TimeUnit.MINUTES)
    @Disabled("Needs AWS supervision.")
    fun `test starting job and tracking progress`() {
        startKoin {
            modules(defaultModule())
            module {
                single<TrainingScriptRunner> {
                    val boundBucketName = get<String?>(named(axonBucketName))
                    if (boundBucketName != null) {
                        EC2TrainingScriptRunner(
                                boundBucketName,
                                InstanceType.T2_MICRO
                        )
                    } else {
                        TODO("Support running outside of AWS. Create a local training script runner")
                    }
                }
            }
        }

        val jobRunner = JobRunner()

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

        val id = jobRunner.startJob(job)

        while (true) {
            val shouldBreak = jobRunner.getProgress(id).attempt().unsafeRunSync().fold(
                {
                    it.printStackTrace()
                    false
                },
                {
                    println(it)
                    it == TrainingScriptProgress.Completed
                })

            if (shouldBreak) {
                break
            }

            Thread.sleep(2000)
        }
    }

    @Test
    @Disabled("Needs AWS supervision.")
    fun `test starting job with example model`() {
        startKoin {
            modules(defaultModule())
            module {
                single<TrainingScriptRunner> {
                    val boundBucketName = get<String?>(named(axonBucketName))
                    if (boundBucketName != null) {
                        EC2TrainingScriptRunner(
                                boundBucketName,
                                InstanceType.T2_MICRO
                        )
                    } else {
                        TODO("Support running outside of AWS. Create a local training script runner")
                    }
                }
            }
        }

        val jobRunner = JobRunner()

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

        jobRunner.startJob(job)
    }
}
