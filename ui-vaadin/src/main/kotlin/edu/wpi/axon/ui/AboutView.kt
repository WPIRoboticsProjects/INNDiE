package edu.wpi.axon.ui

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.h1
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.router.Route

@Route(layout = AxonLayout::class)
class AboutView : KComposite() {
    private val root = ui {
        verticalLayout {
            h1("Created by: Austin & Ryan")
        }
    }
}
