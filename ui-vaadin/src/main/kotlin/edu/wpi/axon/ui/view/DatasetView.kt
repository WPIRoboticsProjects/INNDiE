package edu.wpi.axon.ui.view

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.horizontalLayout
import com.vaadin.flow.router.Route
import edu.wpi.axon.ui.AxonLayout
import edu.wpi.axon.ui.view.composite.DatasetSelector
import edu.wpi.axon.ui.view.composite.JobsList

@Route(layout = AxonLayout::class)
class DatasetView : KComposite() {
    private val root = ui {
        horizontalLayout {
            add(JobsList())
            add(DatasetSelector())
        }
    }
}
