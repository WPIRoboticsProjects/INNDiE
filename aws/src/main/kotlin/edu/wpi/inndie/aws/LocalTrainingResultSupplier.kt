package edu.wpi.inndie.aws

import java.io.File
import java.nio.file.Path

class LocalTrainingResultSupplier : TrainingResultSupplier {

    private val jobDirs = mutableMapOf<Int, Path>()

    fun addJob(id: Int, workingDir: Path) {
        jobDirs[id] = workingDir
    }

    override fun listResults(id: Int): List<String> {
        requireJobIsInMaps(id)
        return jobDirs[id]!!.toFile().listFiles()!!.map { it.name }
    }

    override fun getResult(id: Int, filename: String): File {
        requireJobIsInMaps(id)
        return jobDirs[id]!!.resolve(filename).toFile()
    }

    private fun requireJobIsInMaps(jobId: Int) {
        require(jobId in jobDirs.keys)
    }
}
