package edu.wpi.axon.ui.model

import edu.wpi.axon.tfdata.Dataset

data class Job(var name: String, var state: String, var userDataset: Dataset)
