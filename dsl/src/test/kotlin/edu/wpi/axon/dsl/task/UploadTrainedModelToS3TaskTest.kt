package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture
import edu.wpi.axon.testutil.KoinTestFixture
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

internal class UploadTrainedModelToS3TaskConfigurationTest :
    TaskConfigurationTestFixture<UploadTrainedModelToS3Task>(
        { UploadTrainedModelToS3Task("") },
        listOf()
    )

internal class UploadTrainedModelToS3TaskTest : KoinTestFixture() {

    @Test
    fun `test code gen`() {
        startKoin { }

        val task = UploadTrainedModelToS3Task("").apply {
            modelName = "modelName.h5"
            bucketName = "bucketName"
        }

        task.code() shouldBe """
            |axon.client.impl_upload_trained_model("modelName.h5", "bucketName", None)
        """.trimMargin()
    }
}
