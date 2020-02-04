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

    private val s3 = S3Client.builder().build()

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
     * Lists the training results for the Job.
     *
     * @param jobId The ID of the Job.
     * @return The filenames of the results.
     */
    fun listTrainingResults(jobId: Int): List<String> =
        listObjectsWithPrefixAndRemovePrefix("axon-training-results/$jobId/")

    /**
     * Downloads a training result to a local file.
     *
     * @param jobId The ID of the Job.
     * @param resultFilename The filename of the result to download.
     * @return A local file containing the result.
     */
    fun downloadTrainingResult(jobId: Int, resultFilename: String): File =
        downloadToLocalFile("axon-training-results/$jobId/$resultFilename")

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
     * @param id The unique Job ID.
     * @return The contents of the progress file.
     */
    @UseExperimental(ExperimentalStdlibApi::class)
    fun getTrainingProgress(id: Int): String = s3.getObject {
        it.bucket(bucketName).key(createTrainingProgressFilePath(id))
    }.readAllBytes().decodeToString()

    /**
     * Sets the training progress data.
     *
     * @param id The unique Job ID.
     * @param data The data to write to the progress file.
     */
    fun setTrainingProgress(id: Int, data: String) {
        s3.putObject(
            PutObjectRequest.builder().bucket(bucketName).key(
                createTrainingProgressFilePath(id)
            ).build(),
            RequestBody.fromString(data)
        )
    }

    /**
     * Creates a heartbeat that Axon uses to check if the training script is running properly.
     *
     * @param id The unique Job ID.
     */
    fun createHeartbeat(id: Int) {
        s3.putObject(
            PutObjectRequest.builder().bucket(bucketName).key(
                createHeartbeatFilePath(id)
            ).build(),
            RequestBody.fromString("1")
        )
    }

    /**
     * Removes a heartbeat that Axon uses to check if the training script is running properly.
     *
     * @param id The unique Job ID.
     */
    fun removeHeartbeat(id: Int) {
        s3.putObject(
            PutObjectRequest.builder().bucket(bucketName).key(
                createHeartbeatFilePath(id)
            ).build(),
            RequestBody.fromString("0")
        )
    }

    /**
     * Gets the latest heartbeat.
     *
     * @param id The unique Job ID.
     * @return The contents of the heartbeat file.
     */
    @UseExperimental(ExperimentalStdlibApi::class)
    fun getHeartbeat(id: Int) = s3.getObject {
        it.bucket(bucketName).key(createHeartbeatFilePath(id))
    }.readAllBytes().decodeToString()

    /**
     * Clears the log file from the training script.
     *
     * @param id The unique Job ID.
     */
    fun clearTrainingLogFile(id: Int) {
        s3.putObject(
            PutObjectRequest.builder().bucket(bucketName)
                .key("axon-training-progress/$id/log.txt").build(),
            RequestBody.fromString("")
        )
    }

    /**
     * Gets the log file from the training script.
     *
     * @param id The unique Job ID.
     * @return The contents of the log file.
     */
    @UseExperimental(ExperimentalStdlibApi::class)
    fun getTrainingLogFile(id: Int) = s3.getObject {
        it.bucket(bucketName).key("axon-training-progress/$id/log.txt")
    }.readAllBytes().decodeToString()

    /**
     * Uploads a plugin cache file.
     *
     * @param cacheName The name of the plugin cache.
     * @param file The plugin cache file.
     */
    fun uploadPluginCache(cacheName: String, file: File) =
        uploadLocalFile(file, "axon-plugins/$cacheName/plugin_cache.json")

    /**
     * Downloads a plugin cache file.
     *
     * @param cacheName The name of the plugin cache.
     * @return A local file containing the plugin cache.
     */
    fun downloadPluginCache(cacheName: String): File =
        downloadToLocalFile("axon-plugins/$cacheName/plugin_cache.json")

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

    private fun createTrainingProgressPrefix(id: Int) = "axon-training-progress/$id"

    private fun createTrainingProgressFilePath(id: Int) =
        "${createTrainingProgressPrefix(id)}/progress.txt"

    private fun createHeartbeatFilePath(id: Int) =
        "${createTrainingProgressPrefix(id)}/heartbeat.txt"

    companion object {
        private const val preferencesFilename = "axon-preferences.json"
    }
}
