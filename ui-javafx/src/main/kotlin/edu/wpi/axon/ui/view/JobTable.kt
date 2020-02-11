package edu.wpi.axon.ui.view

import edu.wpi.axon.db.JobDb
import edu.wpi.axon.db.data.Job
import edu.wpi.axon.ui.model.JobModel
import tornadofx.Fragment
import tornadofx.asObservable
import tornadofx.bindSelected
import tornadofx.readonlyColumn
import tornadofx.tableview

class JobTable: Fragment() {

    private val database by di<JobDb>()
    private val model by inject<JobModel>()

    private val list = database.fetchAll().asObservable()

    override val root = tableview(list) {
        bindSelected(model)

        readonlyColumn("Name", Job::name)
    }
}