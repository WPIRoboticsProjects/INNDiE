package edu.wpi.axon.aws

import edu.wpi.axon.util.localCacheDir
import java.io.File
import java.nio.file.Files
import mu.KotlinLogging
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
    private val cacheDir: File = localCacheDir.resolve("s3-cache").toFile().apply { mkdirs() }
    private val modelCacheDir: File = cacheDir.resolve("models").apply { mkdirs() }
    private val trainingResultCacheDir: File = cacheDir.resolve("training-results")
        .apply { mkdirs() }
    private val pluginCacheDir: File = cacheDir.resolve("plugins").apply { mkdirs() }

    /**
     * Uploads a model.
     *
     * @param file The local file containing the model to upload. The filename of the uploaded
     * model will be the same as the filename of this file.
     */
    fun uploadModel(file: File) =
        uploadLocalFile(file, "axon-models/${file.name}")

    /**
     * Downloads a model.
     *
     * @param filename The filename of the model file.
     * @return A local file containing the model.
     */
    fun downloadModel(filename: String): File =
        downloadToLocalFile(modelCacheDir, "axon-models/$filename")

    /**
     * Lists the training results for the Job.
     *
     * @param jobId The ID of the Job.
     * @return The filenames of the results.
     */
    fun listTrainingResults(jobId: Int): List<String> {
        val out = listObjectsWithPrefixAndRemovePrefix("axon-training-results/$jobId/")
        LOGGER.debug { "Training results:\n$out" }
        return out
    }

    /**
     * Downloads a training result to a local file.
     *
     * @param jobId The ID of the Job.
     * @param resultFilename The filename of the result to download.
     * @return A local file containing the result.
     */
    fun downloadTrainingResult(jobId: Int, resultFilename: String): File =
        downloadToLocalFile(trainingResultCacheDir, "axon-training-results/$jobId/$resultFilename")

    /**
     * Uploads a test data file.
     *
     * @param file The local test data file.
     */
    fun uploadTestDataFile(file: File) = uploadLocalFile(file, "axon-test-data/${file.name}")

    /**
     * Lists all the model files.
     *
     * @return A list of the model filenames.
     */
    fun listModels(): List<String> =
        listObjectsWithPrefixAndRemovePrefix("axon-models/")

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
        downloadToLocalFile(pluginCacheDir, "axon-plugins/$cacheName/plugin_cache.json")

    /**
     * Downloads the preferences file to a local file. Throws an exception if there is no
     * preferences file in S3.
     *
     * @return A local file containing the preferences.
     */
    @UseExperimental(ExperimentalStdlibApi::class)
    internal fun downloadPreferences(): File {
        val data = s3.getObject { it.bucket(bucketName).key(preferencesFilename) }.readAllBytes()
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
        // TODO: Add caching here as well by checking the modified dates like we should do in
        //  downloadToLocalFile.
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
    private fun downloadToLocalFile(dir: File, path: String): File {
        val localFile = File(dir, path.substringAfterLast('/'))

        // If the file is already in the cache, don't download anything.
        // TODO: Check the modified timestamp on the local file and on the key in S3 and overwrite
        //  the local file if the key in S3 has been modified more recently.
        if (localFile.exists()) {
            return localFile
        }

        val data = s3.getObject {
            it.bucket(bucketName).key(path)
        }.readAllBytes()
        localFile.writeBytes(data)
        return localFile
    }

    /**
     * @return A list of all objects in S3 matching the [prefix], with the [prefix] removed from
     * their keys.
     */
    private fun listObjectsWithPrefixAndRemovePrefix(prefix: String) =
        s3.listObjects {
            it.bucket(bucketName).prefix(prefix).maxKeys(1000)
        }.contents().map { it.key().substring(prefix.length) }

    private fun createTrainingProgressPrefix(id: Int) = "axon-training-progress/$id"

    private fun createTrainingProgressFilePath(id: Int) =
        "${createTrainingProgressPrefix(id)}/progress.txt"

    private fun createHeartbeatFilePath(id: Int) =
        "${createTrainingProgressPrefix(id)}/heartbeat.txt"

    companion object {
        private val LOGGER = KotlinLogging.logger { }
        private const val preferencesFilename = "axon-preferences.json"
    }
}
