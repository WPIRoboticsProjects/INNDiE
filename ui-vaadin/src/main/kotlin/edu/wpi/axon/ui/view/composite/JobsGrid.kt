package edu.wpi.axon.ui.view.composite

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.addColumnFor
import com.github.mvysny.karibudsl.v10.grid
import com.github.mvysny.karibudsl.v10.gridContextMenu
import com.github.mvysny.karibudsl.v10.item
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.ColumnTextAlign
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.TextRenderer
import com.vaadin.flow.function.SerializableFunction
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.ui.service.JobService

class JobsGrid : KComposite() {
    private lateinit var grid: Grid<Job>

    private val root = ui {
        verticalLayout {
            grid = grid<Job>(JobService.dataProvider) {
                addColumnFor(Job::name)
                addColumnFor(Job::status, TextRenderer { it.status.javaClass.simpleName })
                addColumnFor(Job::userDataset, TextRenderer { it.userDataset.displayName })

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
                    item("Clone", { if (it != null) Notification.show("Clone Context: ${it.name}") })
                    item("Run", { if (it != null) Notification.show("Run Context: ${it.name}") })
                    item("Remove", { if (it != null) deleteJob(it) })
                }
            }
        }
    }

    private fun deleteJob(job: Job) {
        JobService.jobs.remove(job.id)
        grid.dataProvider.refreshAll()
    }
}
