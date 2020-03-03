package edu.wpi.axon.aws

import java.io.File

interface TrainingResultSupplier {

    /**
     * Lists the results from running the training script.
     *
     * @param id The Job ID.
     * @return The names of the results.
     */
    fun listResults(id: Int): List<String>

    /**
     * Gets a result as a local file.
     *
     * @param id The Job ID.
     * @param filename The name of the result to download.
     * @return A local file containing the result.
     */
    fun getResult(id: Int, filename: String): File
}
