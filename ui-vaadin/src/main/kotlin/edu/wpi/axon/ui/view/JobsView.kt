package edu.wpi.axon.ui.view

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.addColumnFor
import com.github.mvysny.karibudsl.v10.flexGrow
import com.github.mvysny.karibudsl.v10.grid
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.ColumnTextAlign
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.TextRenderer
import com.vaadin.flow.function.SerializableFunction
import com.vaadin.flow.router.Route
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.ui.AxonLayout
import edu.wpi.axon.ui.model.Job

@Route(layout = AxonLayout::class)
class JobsView : KComposite() {
    private val jobs = listOf(
            Job("Job A", "Not Started", Dataset.FashionMnist),
            Job("Job B", "Training", Dataset.Cifar10),
            Job("Job C", "Finished", Dataset.Cifar100),
            Job("Job D", "Hidden", Dataset.Reuters)
    )

    private val root = ui {
        grid<Job>(dataProvider = ListDataProvider(jobs)) {
            flexGrow = 0.0
            addColumnFor(Job::name)
            addColumnFor(Job::state)
            addColumnFor(Job::userDataset, TextRenderer {
                it.userDataset.name
            })

            addColumn(ComponentRenderer<Button, Job>(SerializableFunction { job ->
                Button("Clone") {
                    Notification.show("Clone Button: $job.name")
                }
            })).apply {
                textAlign = ColumnTextAlign.END
                flexGrow = 0
            }
            addColumn(ComponentRenderer<Button, Job>(SerializableFunction { job ->
                Button("Run") {
                    Notification.show("Run Button: $job.name")
                }
            })).apply {
                textAlign = ColumnTextAlign.END
                flexGrow = 0
            }
            addColumn(ComponentRenderer<Button, Job>(SerializableFunction { job ->
                Button("Remove") {
                    Notification.show("Remove Button: $job.name")
                }
            })).apply {
                textAlign = ColumnTextAlign.END
                flexGrow = 0
            }
        }
    }
}
