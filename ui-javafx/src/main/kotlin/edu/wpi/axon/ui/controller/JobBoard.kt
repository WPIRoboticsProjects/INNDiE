package edu.wpi.axon.ui.controller

import edu.wpi.axon.db.JobDb
import edu.wpi.axon.ui.model.JobDto
import tornadofx.Controller
import tornadofx.asObservable

class JobBoard: Controller() {
    private val database by di<JobDb>()

    val jobs by lazy {
        database.fetchAll().map { JobDto(it) }.asObservable()
    }
}