package edu.wpi.axon.ui.view.jobs

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.horizontalLayout
import com.github.mvysny.karibudsl.v10.isExpand
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.github.mvysny.karibudsl.v10.textField
import com.github.mvysny.karibudsl.v10.verticalAlignSelf
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.router.BeforeEvent
import com.vaadin.flow.router.HasUrlParameter
import com.vaadin.flow.router.OptionalParameter
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouteAlias
import edu.wpi.axon.db.JobDb
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.ui.MainLayout
import edu.wpi.axon.ui.service.JobProvider
import edu.wpi.axon.ui.view.EntityView
import org.koin.core.KoinComponent
import org.koin.core.inject

@Route(value = "jobs", layout = MainLayout::class)
@RouteAlias(value = "", layout = MainLayout::class)
class JobsView : KComposite(), HasUrlParameter<String>, EntityView<Job>, KoinComponent {

    private val dataProvider by inject<JobProvider>()
    private val jobDb by inject<JobDb>()

    private lateinit var grid: JobGrid
    private lateinit var form: JobForm

    private val viewLogic = JobsViewLogic(this)

    private val root = ui {
        horizontalLayout {
            setSizeFull()
            verticalLayout {
                setSizeFull()
                horizontalLayout {
                    setWidthFull()
                    textField {
                        verticalAlignSelf = FlexComponent.Alignment.START
                        isExpand = true
                        placeholder = "Filter name"
                    }
                    button("New job", Icon(VaadinIcon.PLUS_CIRCLE)) {
                        addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                        onLeftClick {
                            viewLogic.newJob()
                        }
                    }
                }
                grid = jobGrid(dataProvider) {
                    asSingleSelect().addValueChangeListener {
                        it.value?.let { job ->
                            viewLogic.edit(job)
                        }
                    }
                }
            }
            form = jobForm(viewLogic)
        }
    }

    init {
        viewLogic.clear()
    }

    fun createJob() {
        showEditor()
        form.createJob()
    }

    fun editJob(job: Job) {
        showEditor()
        form.editJob(job)
    }

    fun showEditor() {
        form.isVisible = true
    }

    fun hideEditor() {
        form.isVisible = false
    }

    fun clearSelection() {
        grid.selectionModel.deselectAll()
    }

    fun selectRow(job: Job) {
        grid.selectionModel.select(job)
    }

    fun updateJob(job: Job) {
        jobDb.update(job)
        dataProvider.refreshItem(job)
    }

    override fun setParameter(event: BeforeEvent?, @OptionalParameter parameter: String?) {
        viewLogic.enter(parameter)
    }

    override val entityName: String
        get() = Job::class.java.simpleName
}
