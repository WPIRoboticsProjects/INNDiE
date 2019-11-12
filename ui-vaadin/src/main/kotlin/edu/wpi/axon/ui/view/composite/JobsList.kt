package edu.wpi.axon.ui.view.composite

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.gridContextMenu
import com.github.mvysny.karibudsl.v10.h4
import com.github.mvysny.karibudsl.v10.item
import com.github.mvysny.karibudsl.v10.sortProperty
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.data.provider.DataProvider
import edu.wpi.axon.ui.model.Job
import edu.wpi.axon.ui.temp.JobService

class JobsList : KComposite() {
    private val jobProvider = DataProvider.fromCallbacks<Job>(
            { JobService.fetchJobs(it.offset, it.limit) },
            { JobService.getJobCount() }
    )

    val grid = Grid<Job>()

    private val root = ui {
        verticalLayout {
            h4("Jobs")

            grid.apply {
                dataProvider = jobProvider

                addColumn { Job::name.get(it) }.apply {
                    key = Job::name.name
                    sortProperty = Job::name
                }

                gridContextMenu {
                    item("Clone", { if (it != null) Notification.show("Clone Context: ${it.name}") })
                    item("Run", { if (it != null) Notification.show("Run Context: ${it.name}") })
                    item("Remove", { if (it != null) deleteJob(it) })
                }
            }
        }
    }

    private fun deleteJob(job: Job) {
        JobService.deleteJob(job)
        jobProvider.refreshAll()
    }
}
