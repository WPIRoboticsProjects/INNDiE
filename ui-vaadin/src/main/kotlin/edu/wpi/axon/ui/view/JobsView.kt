package edu.wpi.axon.ui.view

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.router.Route
import edu.wpi.axon.ui.AxonLayout
import edu.wpi.axon.ui.view.composite.JobsGrid

@Route(layout = AxonLayout::class)
class JobsView : KComposite() {
    private val root = ui {
        verticalLayout {
            add(JobsGrid())
        }
    }
}
