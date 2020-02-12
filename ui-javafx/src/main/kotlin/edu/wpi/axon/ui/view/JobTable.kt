package edu.wpi.axon.ui.view

import edu.wpi.axon.ui.controller.JobBoard
import edu.wpi.axon.ui.model.JobDto
import edu.wpi.axon.ui.model.JobModel
import javafx.scene.layout.Priority
import tornadofx.Fragment
import tornadofx.bindSelected
import tornadofx.column
import tornadofx.hgrow
import tornadofx.smartResize
import tornadofx.tableview
import tornadofx.vgrow

class JobTable: Fragment() {
    private val jobBoard by inject<JobBoard>()
    private val model by inject<JobModel>()

    override val root = tableview(jobBoard.jobs) {
        bindSelected(model)

        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS
        smartResize()

        column("ID", JobDto::idProperty)
        column("Name", JobDto::nameProperty)
        column("Status", JobDto::statusProperty).cellFragment(StatusCell::class)
    }
}
