package edu.wpi.axon.aws

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.io.File

/**
 * Manages various calls to S3.
 *
 * @param bucketName The S3 bucket name to use for all S3 API calls.
 * @param region The region to connect to, or `null` to autodetect the region.
 */
class S3Manager(
    private val bucketName: String,
    private val region: Region?
) {

    private val s3 by lazy { S3Client.builder().apply { region?.let { region(it) } }.build() }

    /**
     * Uploads an "untrained" model (one that the user wants to upload to start a job with). Meant
     * to be used as the starting point of a Job.
     *
     * @param file The local file containing the model to upload. The filename of the uploaded
     * model will be the same as the filename of this file.
     */
    fun uploadUntrainedModel(file: File): Unit = TODO()

    /**
     * Downloads an "untrained" model (one that the user uploaded). Meant to be
     * used as the starting point of a Job.
     *
     * @param filename The filename of the model file.
     * @return A local file containing the model.
     */
    fun downloadUntrainedModel(filename: String): File = TODO()

    /**
     * Uploads a trained model (one that the user wants to test with).
     *
     * @param file The local file containing the model to upload. The filename of the uploaded model
     * will be the same as the filename of this file.
     */
    fun uploadTrainedModel(file: File): Unit = TODO()

    /**
     * Downloads a trained model (which was put in S3 by the training script when it
     * ran on EC2). Meant to be used to download to the user's local machine.
     *
     * @param filename The filename of the trained model file.
     * @return A local file containing the trained model.
     */
    fun downloadTrainedModel(filename: String): File = TODO()

    /**
     * Uploads a test data file.
     *
     * @param file The local test data file.
     */
    fun uploadTestDataFile(file: File): Unit = TODO()

    /**
     * Lists all the untrained model files.
     *
     * @return A list of the untrained model filenames.
     */
    fun listUntrainedModels(): List<String> = TODO()

    /**
     * Lists all the test data files.
     *
     * @return A list of the test data filenames.
     */
    fun listTestDataFiles(): List<String> = TODO()
}
