package edu.wpi.axon.ui.view.jobs

import com.vaadin.flow.component.UI
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.ui.service.JobService

class JobsViewLogic(private val view: JobsView) {
    fun enter(parameter: String?) {
        when {
            parameter == null || parameter.isEmpty() -> {
                view.hideEditor()
            }
            parameter == "new" -> {
                newJob()
            }
            else -> {
                parameter.toIntOrNull()?.let {
                    JobService.jobs.getById(it)?.let { job ->
                        view.selectRow(job)
                    }
                }
            }
        }
    }

    fun save(job: Job) {
        view.clearSelection()
        view.updateJob(job)
        UI.getCurrent().navigate(JobsView::class.java)
        view.showUpdatedNotification()
    }

    fun clear() {
        UI.getCurrent().navigate(JobsView::class.java)
        view.clearSelection()
    }

    fun edit(job: Job) {
        UI.getCurrent().navigate(JobsView::class.java, job.id.toString())
        view.editJob(job)
    }

    fun newJob() {
        UI.getCurrent().navigate(JobsView::class.java, "new")
        view.createJob()
    }
}
