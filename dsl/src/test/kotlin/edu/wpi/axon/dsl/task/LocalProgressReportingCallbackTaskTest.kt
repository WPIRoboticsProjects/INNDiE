package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture
import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.dsl.mockVariableNameGenerator
import edu.wpi.axon.testutil.KoinTestFixture
import io.kotlintest.shouldBe
import kotlin.random.Random
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal class LocalProgressReportingCallbackTaskConfigurationTest :
    TaskConfigurationTestFixture<LocalProgressReportingCallbackTask>(
        {
            LocalProgressReportingCallbackTask("").apply {
                jobId = Random.nextInt(1, Int.MAX_VALUE)
            }
        },
        listOf(
            LocalProgressReportingCallbackTask::output
        )
    )

internal class LocalProgressReportingCallbackTaskTest : KoinTestFixture() {

    @Test
    fun `test code gen`() {
        startKoin {
            modules(module { mockVariableNameGenerator() })
        }

        val task = LocalProgressReportingCallbackTask("").apply {
            jobId = Random.nextInt(1, Int.MAX_VALUE)
            output = configuredCorrectly("output")
        }

        task.code().shouldBe(
            """
            |class var1(tf.keras.callbacks.Callback):
            |    def __init__(self):
            |        super.__init__()
            |        try:
            |            os.makedirs(Path("/tmp/m/d/progress.txt").parent)
            |        except OSError as err:
            |            if err.errno != errno.EEXIST:
            |                raise
            |
            |    def on_epoch_end(self, epoch, logs=None):
            |        with open("/tmp/${task.jobId}/progress.txt", "w") as f:
            |            f.write(str(epoch + 1))
            |
            |output = var1()
            """.trimMargin()
        )
    }
}
