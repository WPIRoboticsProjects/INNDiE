package edu.wpi.axon.ui.view.composite

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.addColumnFor
import com.github.mvysny.karibudsl.v10.grid
import com.github.mvysny.karibudsl.v10.gridContextMenu
import com.github.mvysny.karibudsl.v10.item
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.data.renderer.TextRenderer
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.ui.service.JobService

class JobsGrid : KComposite() {
    private val root = ui {
        verticalLayout {
            grid<Job>(dataProvider = JobService.dataProvider) {
                addColumnFor(Job::name)
                addColumnFor(Job::status)
                addColumnFor(Job::userDataset, TextRenderer { it.userDataset.displayName })

                gridContextMenu {
                    item(
                        "Clone",
                        { if (it != null) Notification.show("Clone Context: ${it.name}") }
                    )
                    item("Run", { if (it != null) Notification.show("Run Context: ${it.name}") })
                    item("Remove", { if (it != null) deleteJob(it) })
                }
            }
        }
    }

    private fun deleteJob(job: Job) {
        JobService.jobs.remove(job.id)
        JobService.dataProvider.refreshAll()
    }
}
