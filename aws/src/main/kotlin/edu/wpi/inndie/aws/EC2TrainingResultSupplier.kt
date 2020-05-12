package edu.wpi.inndie.aws

import java.io.File

class EC2TrainingResultSupplier(private val s3Manager: S3Manager) :
    TrainingResultSupplier {

    override fun listResults(id: Int): List<String> = s3Manager.listTrainingResults(id)

    override fun getResult(id: Int, filename: String): File =
        s3Manager.downloadTrainingResult(id, filename)
}
