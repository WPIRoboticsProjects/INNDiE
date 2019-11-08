package edu.wpi.axon.awsdb

// TODO: What else does a job have?
data class Job(
    val name: String,
    val dataset: String,
    val data: Int // TODO: Remove this
)
