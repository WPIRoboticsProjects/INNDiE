package edu.wpi.axon.ui.model

import edu.wpi.axon.tfdata.Dataset

enum class JobState { NOT_STARTED, IN_PROGRESS, COMPLETED }

data class Job(var name: String, var state: JobState, var dataset: Dataset)


