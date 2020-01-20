package edu.wpi.axon.aws

import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import java.nio.file.Files

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
    fun uploadUntrainedModel(file: File) =
        uploadLocalFile(file, "axon-untrained-models/${file.name}")

    /**
     * Downloads an "untrained" model (one that the user uploaded). Meant to be
     * used as the starting point of a Job.
     *
     * @param filename The filename of the model file.
     * @return A local file containing the model.
     */
    fun downloadUntrainedModel(filename: String): File =
        downloadToLocalFile("axon-untrained-models/$filename")

    /**
     * Uploads a trained model (one that the user wants to test with).
     *
     * @param file The local file containing the model to upload. The filename of the uploaded model
     * will be the same as the filename of this file.
     */
    fun uploadTrainedModel(file: File) = uploadLocalFile(file, "axon-trained-models/${file.name}")

    /**
     * Downloads a trained model (which was put in S3 by the training script when it
     * ran on EC2). Meant to be used to download to the user's local machine.
     *
     * @param filename The filename of the trained model file.
     * @return A local file containing the trained model.
     */
    fun downloadTrainedModel(filename: String): File =
        downloadToLocalFile("axon-untrained-models/$filename")

    /**
     * Uploads a test data file.
     *
     * @param file The local test data file.
     */
    fun uploadTestDataFile(file: File) = uploadLocalFile(file, "axon-test-data/${file.name}")

    /**
     * Lists all the untrained model files.
     *
     * @return A list of the untrained model filenames.
     */
    fun listUntrainedModels(): List<String> =
        listObjectsWithPrefixAndRemovePrefix("axon-untrained-models/")

    /**
     * Lists all the test data files.
     *
     * @return A list of the test data filenames.
     */
    fun listTestDataFiles(): List<String> =
        listObjectsWithPrefixAndRemovePrefix("axon-test-data/")

    private fun uploadLocalFile(file: File, path: String) {
        s3.putObject(
            PutObjectRequest.builder().bucket(bucketName).key(path).build(),
            RequestBody.fromFile(file)
        )
    }

    private fun downloadToLocalFile(path: String): File {
        val data = s3.getObject {
            it.bucket(bucketName).key(path)
        }.readAllBytes()
        val localFile = Files.createTempFile("", "").toFile()
        localFile.writeBytes(data)
        return localFile
    }

    private fun listObjectsWithPrefixAndRemovePrefix(prefix: String) =
        s3.listObjects {
            it.bucket(bucketName).prefix(prefix)
        }.contents().map { it.key().substring(prefix.length) }
}
