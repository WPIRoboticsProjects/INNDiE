package edu.wpi.axon.ui.view.jobs

import com.github.mvysny.karibudsl.v10.VaadinDsl
import com.github.mvysny.karibudsl.v10.addColumnFor
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.init
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.github.mvysny.karibudsl.v10.textArea
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.TextRenderer
import edu.wpi.axon.db.data.Job
import edu.wpi.axon.db.data.TrainingScriptProgress

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
            Span(getStatusComponent(job), TrainingProgressBar(job.status))
        }) {
            flexGrow = 10
        }
        addColumnFor(Job::userDataset, TextRenderer { it.userDataset.displayName }) {
            flexGrow = 10
        }
    }

    private fun getStatusComponent(job: Job): Component = when (val status = job.status) {
        is TrainingScriptProgress.Error -> {
            Span().apply {
                add(Text(job.status.javaClass.simpleName))

                add(
                    Button("", Icon(VaadinIcon.INFO).apply { setSize("1.2em") }).apply {
                        width = "1.5em"
                        minWidth = "1.5em"
                        maxWidth = "1.5em"
                        height = "1.5em"
                        minHeight = "1.5em"
                        maxHeight = "1.5em"
                        style["margin-left"] = "0.5em"

                        onLeftClick {
                            Dialog().apply {
                                height = "calc(100vh - (2*var(--lumo-space-m)))"
                                width = "calc(100vw - (4*var(--lumo-space-m)))"

                                verticalLayout {
                                    setWidthFull()
                                    textArea {
                                        setWidthFull()
                                        value = status.log
                                    }

                                    button("Close") {
                                        onLeftClick { close() }
                                    }
                                }

                                open()
                            }
                        }
                    }
                )
            }
        }

        else -> Text(job.status.javaClass.simpleName)
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
