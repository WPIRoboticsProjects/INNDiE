package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.TaskConfigurationTestFixture
import edu.wpi.axon.testutil.KoinTestFixture
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

internal class UploadModelToS3TaskConfigurationTest :
    TaskConfigurationTestFixture<UploadModelToS3Task>(
        { UploadModelToS3Task("") },
        listOf()
    )

internal class UploadModelToS3TaskTest : KoinTestFixture() {

    @Test
    fun `test code gen`() {
        startKoin { }

        val task = UploadModelToS3Task("").apply {
            modelName = "modelName.h5"
            bucketName = "bucketName"
            region = "region-name"
        }

        task.code() shouldBe """
            |axonawsclient.impl_upload_model_file("modelName.h5", "bucketName", "region-name")
        """.trimMargin()
    }
}
