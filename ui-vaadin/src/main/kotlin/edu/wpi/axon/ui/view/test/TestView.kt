package edu.wpi.axon.ui.view.test

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import edu.wpi.axon.ui.MainLayout

@Route(layout = MainLayout::class)
@PageTitle("Test")
class TestView : KComposite() {
    init {
        ui {
            verticalLayout {

            }
        }
    }
}
