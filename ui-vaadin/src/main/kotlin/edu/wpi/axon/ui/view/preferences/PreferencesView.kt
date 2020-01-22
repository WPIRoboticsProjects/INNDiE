package edu.wpi.axon.ui.view.preferences

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.h1
import com.github.mvysny.karibudsl.v10.horizontalLayout
import com.github.mvysny.karibudsl.v10.icon
import com.github.mvysny.karibudsl.v10.text
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.shrinkwrap.VaadinCoreShrinkWrap
import edu.wpi.axon.ui.MainLayout

@Route(value = "preferences", layout = MainLayout::class)
@PageTitle("Preferences")
class PreferencesView : KComposite() {
    private val root = ui {
        verticalLayout {

        }
    }
}
