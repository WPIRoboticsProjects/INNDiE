package edu.wpi.axon.ui.view.composite

import com.github.mvysny.karibudsl.v10.KComposite
import com.vaadin.flow.data.binder.BeanValidationBinder
import edu.wpi.axon.ui.model.Job

open class JobComposite : KComposite() {
    protected val binder = BeanValidationBinder<Job>(Job::class.java)

    fun setJob(job: Job) {
        binder.bean = job
    }
}
