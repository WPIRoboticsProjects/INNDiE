package edu.wpi.axon.aws

import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.util.FilePath
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertEquals
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import software.amazon.awssdk.services.ec2.model.InstanceStateName
import software.amazon.awssdk.services.ec2.model.InstanceType

internal class EC2TrainingScriptRunnerTest {

    private val runner = EC2TrainingScriptRunner(
        InstanceType.T2_MICRO,
        mockk(),
        mockk()
    )

    @Test
    fun `test starting script and getting the state until completion`() {
        val config = randomRunTrainingScriptConfigurationUsingAWS()
        val instanceId = RandomStringUtils.randomAlphanumeric(10)
        val instanceType = InstanceType.T2_MICRO

        val ec2 = mockk<EC2Manager> {
            every { startTrainingInstance(any(), any()) } returns instanceId
            every { getInstanceState(instanceId) } returnsMany listOf(
                InstanceStateName.PENDING, // Call 1
                InstanceStateName.RUNNING, // Call 2
                InstanceStateName.SHUTTING_DOWN // Call 3
            )
        }

        val s3Manager = mockk<S3Manager> {
            every { uploadTrainingScript(any(), any()) } returns Unit
            every { setTrainingProgress(any(), any()) } returns Unit
            every { removeHeartbeat(any()) } returns Unit
            every { getHeartbeat(any()) } returnsMany listOf(
                "0", // Call 1
                "1", // Call 2
                "0" // Call 3
            )
            every { getTrainingProgress(any()) } returnsMany listOf(
                "not started", // Call 1
                "1", // Call 2
                "completed" // Call 3
            )
        }

        val runner = EC2TrainingScriptRunner(instanceType, ec2, s3Manager)

        // //////////////////////////////////
        // Starting the instance
        // //////////////////////////////////

        runner.startScript(config)

        verifyScriptStartedCorrectly(s3Manager, config, ec2, instanceType)

        // //////////////////////////////////
        // Progress reporting
        // //////////////////////////////////

        runner.getTrainingProgress(config.id).should {
            it == TrainingScriptProgress.Creating ||
                it == TrainingScriptProgress.Initializing
        }
        runner.getTrainingProgress(config.id)
            .shouldBe(TrainingScriptProgress.InProgress(1 / config.epochs.toDouble()))
        runner.getTrainingProgress(config.id).shouldBe(TrainingScriptProgress.Completed)

        verify(atLeast = 3) {
            ec2.getInstanceState(instanceId)
        }

        verify(atLeast = 3) {
            s3Manager.getHeartbeat(config.id)
        }

        verify(atLeast = 3) {
            s3Manager.getTrainingProgress(config.id)
        }
    }

    @Test
    fun `test starting the script and cancelling it`() {
        val config = randomRunTrainingScriptConfigurationUsingAWS()
        val instanceId = RandomStringUtils.randomAlphanumeric(10)
        val instanceType = InstanceType.T2_MICRO

        val ec2 = mockk<EC2Manager> {
            every { startTrainingInstance(any(), any()) } returns instanceId
            every { terminateInstance(instanceId) } returns Unit
        }

        val s3Manager = mockk<S3Manager> {
            every { uploadTrainingScript(any(), any()) } returns Unit
            every { setTrainingProgress(any(), any()) } returns Unit
            every { removeHeartbeat(any()) } returns Unit
        }

        val runner = EC2TrainingScriptRunner(instanceType, ec2, s3Manager)

        // //////////////////////////////////
        // Starting the instance
        // //////////////////////////////////

        runner.startScript(config)

        verifyScriptStartedCorrectly(s3Manager, config, ec2, instanceType)

        // //////////////////////////////////
        // Cancelling the script
        // //////////////////////////////////

        runner.cancelScript(config.id)

        verify(exactly = 1) {
            ec2.terminateInstance(instanceId)
        }
    }

    private fun verifyScriptStartedCorrectly(
        s3Manager: S3Manager,
        config: RunTrainingScriptConfiguration,
        ec2: EC2Manager,
        instanceType: InstanceType
    ) {
        verify(exactly = 1) {
            s3Manager.uploadTrainingScript(any(), any())
        }

        verify(exactly = 1) {
            s3Manager.setTrainingProgress(config.id, "not started")
        }

        verify(exactly = 1) {
            s3Manager.removeHeartbeat(config.id)
        }

        verify(exactly = 1) {
            ec2.startTrainingInstance(any(), instanceType)
        }
    }

    @Test
    fun `test running with local old model`() {
        shouldThrow<IllegalArgumentException> {
            runner.startScript(
                RunTrainingScriptConfiguration(
                    FilePath.Local("a"),
                    FilePath.S3("b"),
                    Dataset.ExampleDataset.FashionMnist,
                    "",
                    1,
                    1
                )
            )
        }
    }

    @Test
    fun `test running with local new model`() {
        shouldThrow<IllegalArgumentException> {
            runner.startScript(
                RunTrainingScriptConfiguration(
                    FilePath.S3("a"),
                    FilePath.Local("b"),
                    Dataset.ExampleDataset.FashionMnist,
                    "",
                    1,
                    1
                )
            )
        }
    }

    @Test
    fun `test running with zero epochs`() {
        shouldThrow<IllegalArgumentException> {
            runner.startScript(
                RunTrainingScriptConfiguration(
                    FilePath.S3("a"),
                    FilePath.S3("b"),
                    Dataset.ExampleDataset.FashionMnist,
                    "",
                    0,
                    1
                )
            )
        }
    }

    @Test
    fun `test running with local dataset`() {
        shouldThrow<IllegalArgumentException> {
            runner.startScript(
                RunTrainingScriptConfiguration(
                    FilePath.S3("a"),
                    FilePath.S3("b"),
                    Dataset.Custom(FilePath.Local("d"), "d"),
                    "",
                    1,
                    1
                )
            )
        }
    }

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
            EC2TrainingScriptRunner.computeTrainingScriptProgress(
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
