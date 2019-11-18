package edu.wpi.axon.ui.view

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.addColumnFor
import com.github.mvysny.karibudsl.v10.grid
import com.github.mvysny.karibudsl.v10.gridContextMenu
import com.github.mvysny.karibudsl.v10.item
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.ColumnTextAlign
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.TextRenderer
import com.vaadin.flow.function.SerializableFunction
import com.vaadin.flow.router.Route
import edu.wpi.axon.ui.AxonLayout
import edu.wpi.axon.ui.model.Job
import edu.wpi.axon.ui.temp.JobService

@Route(layout = AxonLayout::class)
class JobsView : KComposite() {
    private val dataProvider = DataProvider.fromCallbacks<Job>(
        { JobService.fetchJobs(it.offset, it.limit) },
        { JobService.getJobCount() }
    )

    private val root = ui {
        verticalLayout {
            grid<Job>(dataProvider = dataProvider) {
                addColumnFor(Job::name)
                addColumnFor(Job::state)
                addColumnFor(Job::dataset, TextRenderer { it.dataset.displayName })

                addColumn(ComponentRenderer<Button, Job>(SerializableFunction { job ->
                    Button("Clone") {
                        Notification.show("Clone Button: ${job.name}")
                    }
                })).apply {
                    textAlign = ColumnTextAlign.END
                }
                addColumn(ComponentRenderer<Button, Job>(SerializableFunction { job ->
                    Button("Run") {
                        Notification.show("Run Button: ${job.name}")
                    }
                })).apply {
                    textAlign = ColumnTextAlign.END
                }
                addColumn(ComponentRenderer<Button, Job>(SerializableFunction { job ->
                    Button("Remove") { deleteJob(job) }
                })).apply {
                    textAlign = ColumnTextAlign.END
                }

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
        JobService.deleteJob(job)
        dataProvider.refreshAll()
    }
}
