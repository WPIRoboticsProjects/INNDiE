package edu.wpi.axon.aws

import java.io.File
import java.nio.file.Files
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

/**
 * Manages various calls to S3.
 *
 * @param bucketName The S3 bucket name to use for all S3 API calls.
 */
class S3Manager(
    private val bucketName: String
) {

    private val s3 by lazy { S3Client.builder().build() }

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
        it.bucket(bucketName).key(createTrainingProgressFilePath(modelName, datasetName))
    }.readAllBytes().decodeToString()

    /**
     * Resets the latest training progress data.
     *
     * @param modelName The filename of the model being trained.
     * @param datasetName The filename of the dataset being trained on.
     */
    fun resetTrainingProgress(modelName: String, datasetName: String) {
        s3.deleteObject {
            it.bucket(bucketName).key(createTrainingProgressFilePath(modelName, datasetName))
        }
    }

    /**
     * Downloads the preferences file to a local file. Throws an exception if there is no
     * preferences file in S3.
     *
     * @return A local file containing the preferences.
     */
    @UseExperimental(ExperimentalStdlibApi::class)
    internal fun downloadPreferences(): File {
        val data = s3.getObject { it.key(preferencesFilename) }.readAllBytes()
        val file = Files.createTempFile("", "").toFile()
        file.writeBytes(data)
        return file
    }

    /**
     * Uploads a preferences file to S3.
     *
     * @param file The local preferences file to upload.
     */
    internal fun uploadPreferences(file: File) {
        s3.putObject(
            PutObjectRequest.builder().bucket(bucketName).key(preferencesFilename).build(),
            RequestBody.fromFile(file)
        )
    }

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

    private fun createTrainingProgressFilePath(modelName: String, datasetName: String) =
        "axon-training-progress/$modelName/$datasetName/progress.txt"

    companion object {
        private const val preferencesFilename = "axon-preferences.json"
    }
}