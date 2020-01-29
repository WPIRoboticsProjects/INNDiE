package edu.wpi.axon.aws

import edu.wpi.axon.db.data.TrainingScriptProgress
import kotlin.test.assertEquals
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
        assertEquals(
            expected,
            EC2TrainingScriptProgressReporter.computeTrainingScriptProgress(
                heartbeat,
                progress,
                status,
                epochs
            )
        )
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        fun progressTestSource() = listOf(
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
                "1",
                "initializing",
                InstanceStateName.RUNNING,
                1,
                TrainingScriptProgress.Initializing
            ),
            Arguments.of(
                "1",
                "1.0",
                InstanceStateName.RUNNING,
                1,
                TrainingScriptProgress.InProgress(1.0)
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
                TrainingScriptProgress.Error
            ),
            Arguments.of(
                "1",
                "not started",
                InstanceStateName.PENDING,
                1,
                TrainingScriptProgress.Error
            ),
            Arguments.of(
                "1",
                "not started",
                InstanceStateName.RUNNING,
                1,
                TrainingScriptProgress.Error
            ),
            Arguments.of("1", "not started", null, 1, TrainingScriptProgress.Error),
            Arguments.of("1", "1.0", InstanceStateName.STOPPING, 1, TrainingScriptProgress.Error),
            Arguments.of("1", "1.0", InstanceStateName.TERMINATED, 1, TrainingScriptProgress.Error),
            Arguments.of(
                "0",
                "initializing",
                InstanceStateName.RUNNING,
                1,
                TrainingScriptProgress.Error
            ),
            Arguments.of(
                "1",
                "initializing",
                InstanceStateName.SHUTTING_DOWN,
                1,
                TrainingScriptProgress.Error
            ),
            Arguments.of(
                "2",
                "initializing",
                InstanceStateName.RUNNING,
                1,
                TrainingScriptProgress.Error
            ),
            Arguments.of("1", "foo", InstanceStateName.RUNNING, 1, TrainingScriptProgress.Error)
        )
    }
}
