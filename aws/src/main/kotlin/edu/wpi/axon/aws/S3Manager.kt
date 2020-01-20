package edu.wpi.axon.aws

import java.io.File
import java.nio.file.Files
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

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
        downloadToLocalFile("axon-trained-models/$filename")

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

    /**
     * Uploads a training script.
     *
     * @param scriptFilename The filename to upload the script contents to.
     * @param scriptContents The contents of the script.
     */
    fun uploadTrainingScript(scriptFilename: String, scriptContents: String) {
        s3.putObject(
            PutObjectRequest.builder().bucket(bucketName)
                .key("axon-training-scripts/$scriptFilename").build(),
            RequestBody.fromString(scriptContents)
        )
    }

    /**
     * Gets the latest training progress data.
     *
     * @param modelName The filename of the model being trained.
     * @param datasetName The filename of the dataset being trained on.
     * @return The contents of the progress file.
     */
    @UseExperimental(ExperimentalStdlibApi::class)
    fun getTrainingProgress(modelName: String, datasetName: String): String = s3.getObject {
        it.bucket(bucketName).key("axon-training-progress/$modelName/$datasetName/progress.txt")
    }.readAllBytes().decodeToString()

    /**
     * Uploads a local file to S3.
     *
     * @param file The local file to upload.
     * @param path The path in S3 to upload to.
     */
    private fun uploadLocalFile(file: File, path: String) {
        s3.putObject(
            PutObjectRequest.builder().bucket(bucketName).key(path).build(),
            RequestBody.fromFile(file)
        )
    }

    /**
     * Downloads a file from S3 into a local file. The name of the local file will be the same as
     * the name of the file in S3 (the prefix is stripped from the [path]).
     *
     * @param path The path in S3 to download from.
     * @return A local file containing the data from the file in S3.
     */
    private fun downloadToLocalFile(path: String): File {
        val data = s3.getObject {
            it.bucket(bucketName).key(path)
        }.readAllBytes()

        // Put the downloaded file in a temp dir to ensure it doesn't overwrite anything
        val tempDir = Files.createTempDirectory("")

        val localFile = File(tempDir.toFile(), path.substringAfterLast('/'))
        check(localFile.createNewFile()) {
            "File ${localFile.absolutePath} already existed but should not have. Not going to " +
                "overwrite with new data."
        }

        localFile.writeBytes(data)
        return localFile
    }

    /**
     * @return A list of all objects in S3 matching the [prefix], with the [prefix] removed from
     * their keys.
     */
    private fun listObjectsWithPrefixAndRemovePrefix(prefix: String) =
        s3.listObjects {
            it.bucket(bucketName).prefix(prefix)
        }.contents().map { it.key().substring(prefix.length) }
}
