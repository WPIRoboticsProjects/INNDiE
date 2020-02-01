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
import java.nio.file.Paths
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
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
                    Dataset.ExampleDataset.FashionMnist,
                    "",
                    1,
                    Paths.get("."),
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
                    Dataset.ExampleDataset.FashionMnist,
                    "",
                    0,
                    Paths.get("."),
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
                    Dataset.Custom(FilePath.Local("d"), "d"),
                    "",
                    1,
                    Paths.get("."),
                    1
                )
            )
        }
    }
}
