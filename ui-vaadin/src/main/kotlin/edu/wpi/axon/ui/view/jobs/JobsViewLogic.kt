// package edu.wpi.axon.ui.view.jobs
//
// import com.vaadin.flow.component.UI
// import edu.wpi.axon.db.JobDb
// import edu.wpi.axon.dbdata.Job
// import edu.wpi.axon.dbdata.TrainingScriptProgress
// import edu.wpi.axon.ui.JobRunner
// import kotlin.concurrent.thread
// import org.koin.core.KoinComponent
// import org.koin.core.inject
//
// class JobsViewLogic(private val view: JobsView) : KoinComponent {
//
//    private val jobDb by inject<JobDb>()
//    private val jobRunner by inject<JobRunner>()
//
//    fun enter(parameter: String?) {
//        when {
//            parameter == null || parameter.isEmpty() -> {
//                view.hideEditor()
//            }
//            parameter == "new" -> {
//                newJob()
//            }
//            else -> {
//                parameter.toIntOrNull()?.let {
//                    jobDb.getById(it)?.let { job ->
//                        view.selectRow(job)
//                    }
//                }
//            }
//        }
//    }
//
//    fun save(job: Job) {
//        view.clearSelection()
//        view.updateJob(job)
//        UI.getCurrent().navigate(JobsView::class.java)
//        view.showUpdatedNotification()
//    }
//
//    fun clear() {
//        UI.getCurrent().navigate(JobsView::class.java)
//        view.clearSelection()
//    }
//
//    fun edit(job: Job) {
//        UI.getCurrent().navigate(JobsView::class.java, job.id.toString())
//        view.editJob(job)
//    }
//
//    fun newJob() {
//        UI.getCurrent().navigate(JobsView::class.java, "new")
//        view.createJob()
//    }
//
//    fun runJob(job: Job) {
//        thread(isDaemon = true) {
//            val id = jobRunner.startJob(job)
//
//            while (true) {
//                val shouldBreak = jobRunner.getProgress(id).attempt().unsafeRunSync().fold(
//                        {
//                            view.showError("Could not get Job Status", false)
//                            it.printStackTrace()
//                            false
//                        },
//                        {
//                            jobDb.update(job.copy(status = it))
//                            it == TrainingScriptProgress.Completed
//                        })
//
//                if (shouldBreak) {
//                    break
//                }
//
//                Thread.sleep(2000)
//            }
//        }
//        clear()
//        view.showNotification("Job Started")
//    }
// }
