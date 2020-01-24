package edu.wpi.axon.ui.view.jobs

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.horizontalLayout
import com.github.mvysny.karibudsl.v10.isExpand
import com.github.mvysny.karibudsl.v10.navigateToView
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.github.mvysny.karibudsl.v10.refresh
import com.github.mvysny.karibudsl.v10.textField
import com.github.mvysny.karibudsl.v10.verticalAlignSelf
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.router.AfterNavigationEvent
import com.vaadin.flow.router.AfterNavigationObserver
import com.vaadin.flow.router.BeforeEvent
import com.vaadin.flow.router.HasUrlParameter
import com.vaadin.flow.router.OptionalParameter
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouteAlias
import edu.wpi.axon.db.JobDb
import edu.wpi.axon.db.JobDbOp
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.ui.MainLayout
import edu.wpi.axon.ui.service.JobProvider
import edu.wpi.axon.ui.view.EntityView
import org.koin.core.KoinComponent
import org.koin.core.inject

@Route(layout = MainLayout::class)
@RouteAlias(value = "", layout = MainLayout::class)
class JobsView : KComposite(), HasUrlParameter<Int>, AfterNavigationObserver, EntityView<Job>,
    KoinComponent {

    private val dataProvider = JobProvider()
    private val jobDb by inject<JobDb>()

    override val entityName: String
        get() = Job::class.java.simpleName

    private lateinit var grid: JobGrid
    private lateinit var form: JobEditorForm

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
                            // navigateTo(-1)
                            JobCreatorDialog().open()
                        }
                    }
                }
                grid = jobGrid(dataProvider) {
                    asSingleSelect().addValueChangeListener {
                        navigateTo(it.value?.id)
                    }
                }
            }
            form = jobEditorForm()
        }
    }

    init {
        val ui = UI.getCurrent()
        jobDb.subscribe { op, jobFromDb ->
            form.job.map { currentJob ->
                if (currentJob.id == jobFromDb.id) {
                    ui.access {
                        form.job = if (op == JobDbOp.Remove) None else Some(jobFromDb)
                    }
                }
            }
        }
    }

    override fun setParameter(event: BeforeEvent?, @OptionalParameter jobId: Int?) {
        form.job = Option.fromNullable(jobId?.let { jobDb.getById(it) })
    }

    override fun afterNavigation(event: AfterNavigationEvent) {
        grid.refresh()
    }

    companion object {
        fun navigateTo(jobId: Int? = null) = navigateToView(JobsView::class, jobId)
    }
}
