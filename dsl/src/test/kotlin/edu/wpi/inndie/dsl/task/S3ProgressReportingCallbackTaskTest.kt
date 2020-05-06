package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.TaskConfigurationTestFixture
import edu.wpi.inndie.dsl.configuredCorrectly
import edu.wpi.inndie.dsl.mockVariableNameGenerator
import edu.wpi.inndie.testutil.KoinTestFixture
import edu.wpi.inndie.util.inndieBucketName
import io.kotlintest.shouldBe
import kotlin.random.Random
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal class S3ProgressReportingCallbackTaskConfigurationTest :
    TaskConfigurationTestFixture<S3ProgressReportingCallbackTask>(
        {
            S3ProgressReportingCallbackTask("").apply {
                jobId = Random.nextInt(1, Int.MAX_VALUE)
                csvLogFile = "out/log.csv"
            }
        },
        listOf(
            S3ProgressReportingCallbackTask::output
        )
    )

internal class S3ProgressReportingCallbackTaskTest : KoinTestFixture() {

    @Test
    fun `test code gen`() {
        startKoin {
            modules(module {
                single(named(inndieBucketName)) { "b" }
                mockVariableNameGenerator()
            })
        }

        val task = S3ProgressReportingCallbackTask("").apply {
            jobId = Random.nextInt(1, Int.MAX_VALUE)
            csvLogFile = "./output/logFile.csv"
            output = configuredCorrectly("output")
        }

        task.code().shouldBe(
            """
            |class var1(tf.keras.callbacks.Callback):
            |    def on_epoch_end(self, epoch, logs=None):
            |        if os.path.isfile("./output/logFile.csv"):
            |            with open("./output/logFile.csv", "r") as f:
            |                axon.client.impl_update_training_progress(${task.jobId}, f.read(),
            |                                                          "b",
            |                                                          None)
            |
            |output = var1()
            """.trimMargin()
        )
    }
}
