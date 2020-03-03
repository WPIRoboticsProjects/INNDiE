package edu.wpi.axon.aws

import edu.wpi.axon.db.data.TrainingScriptProgress
import io.kotlintest.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import software.amazon.awssdk.services.ec2.model.InstanceStateName

internal class EC2TrainingScriptProgressReporterTest {

    @ParameterizedTest
    @MethodSource("progressTestSource")
    fun `test progress`(
        heartbeat: String,
        progress: String,
        status: InstanceStateName?,
        epochs: Int,
        expected: TrainingScriptProgress
    ) {
        EC2TrainingScriptProgressReporter.computeTrainingScriptProgress(
            heartbeat,
            progress,
            status,
            "",
            epochs
        )::class.shouldBe(expected::class)
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        fun progressTestSource(): List<Arguments> {
            val epoch1String = """
                epoch,acc,loss,val_acc,val_loss
                0,0.1,0.19715846958009559,0.1,0.029013563808369145
                1,0.1,0.028015766515557757,0.1,0.018882167398363528
            """.trimIndent()

            return listOf(
                Arguments.of("0", "not started", null, 1, TrainingScriptProgress.Creating),
                Arguments.of(
                    "0", "not started", InstanceStateName.PENDING, 1,
                    TrainingScriptProgress.Creating
                ),
                Arguments.of(
                    "0", "not started", InstanceStateName.RUNNING, 1,
                    TrainingScriptProgress.Initializing
                ),
                Arguments.of(
                    "1", "not started", InstanceStateName.RUNNING, 1,
                    TrainingScriptProgress.Initializing
                ),
                Arguments.of(
                    "1",
                    "initializing",
                    InstanceStateName.RUNNING,
                    1,
                    TrainingScriptProgress.Initializing
                ),
                Arguments.of(
                    "1",
                    epoch1String,
                    InstanceStateName.RUNNING,
                    1,
                    TrainingScriptProgress.InProgress(1.0, epoch1String)
                ),
                Arguments.of("0", "completed", null, 1, TrainingScriptProgress.Completed),
                Arguments.of(
                    "0",
                    "completed",
                    InstanceStateName.STOPPING,
                    1,
                    TrainingScriptProgress.Completed
                ),
                Arguments.of(
                    "0", "not started", InstanceStateName.SHUTTING_DOWN, 1,
                    TrainingScriptProgress.Error("")
                ),
                Arguments.of(
                    "1",
                    "not started",
                    InstanceStateName.PENDING,
                    1,
                    TrainingScriptProgress.Error("")
                ),
                Arguments.of(
                    "1",
                    "not started",
                    InstanceStateName.RUNNING,
                    1,
                    TrainingScriptProgress.Initializing
                ),
                Arguments.of("1", "not started", null, 1, TrainingScriptProgress.Error("")),
                Arguments.of(
                    "1",
                    epoch1String,
                    InstanceStateName.STOPPING,
                    1,
                    TrainingScriptProgress.Error("")
                ),
                Arguments.of(
                    "1",
                    epoch1String,
                    InstanceStateName.TERMINATED,
                    1,
                    TrainingScriptProgress.Error("")
                ),
                Arguments.of(
                    "0",
                    "initializing",
                    InstanceStateName.RUNNING,
                    1,
                    TrainingScriptProgress.Error("")
                ),
                Arguments.of(
                    "1",
                    "initializing",
                    InstanceStateName.SHUTTING_DOWN,
                    1,
                    TrainingScriptProgress.Error("")
                ),
                Arguments.of(
                    "2",
                    "initializing",
                    InstanceStateName.RUNNING,
                    1,
                    TrainingScriptProgress.Error("")
                ),
                Arguments.of(
                    "1",
                    "foo",
                    InstanceStateName.RUNNING,
                    1,
                    TrainingScriptProgress.Error("")
                )
            )
        }
    }
}
