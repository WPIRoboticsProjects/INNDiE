package edu.wpi.axon.ui.view.jobs

import com.github.mvysny.karibudsl.v10.VaadinDsl
import com.github.mvysny.karibudsl.v10.addColumnFor
import com.github.mvysny.karibudsl.v10.init
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.TextRenderer
import edu.wpi.axon.dbdata.Job

class JobGrid : Grid<Job>() {
    init {
        addThemeVariants(GridVariant.LUMO_ROW_STRIPES)
        addColumnFor(Job::id) {
            flexGrow = 1
        }
        addColumnFor(Job::name) {
            flexGrow = 10
        }
        addColumnFor(Job::status, ComponentRenderer<Component, Job> { job ->
            Span(Text(" ${job.status.javaClass.simpleName}"), TrainingProgressBar(job.status))
        }) {
            flexGrow = 10
        }
        addColumnFor(Job::userDataset, TextRenderer { it.userDataset.displayName }) {
            flexGrow = 10
        }
    }
}

@VaadinDsl
fun (@VaadinDsl HasComponents).jobGrid(
    dataProvider: DataProvider<Job, *>? = null,
    block: (@VaadinDsl JobGrid).() -> Unit = {}
) =
    init(JobGrid()) {
        if (dataProvider != null) this.dataProvider = dataProvider
        block()
    }
