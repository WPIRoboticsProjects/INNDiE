package edu.wpi.axon.ui.view

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.h1
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.router.Route
import edu.wpi.axon.ui.AxonLayout

@Route(layout = AxonLayout::class)
class AboutView : KComposite() {
    private val root = ui {
        verticalLayout {
            h1("Created by: Austin & Ryan")
        }
    }
}
