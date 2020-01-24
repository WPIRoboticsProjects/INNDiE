package edu.wpi.axon.ui.view.about

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

@Route(value = "about", layout = MainLayout::class)
@PageTitle("About")
class AboutView : KComposite() {
    private val root = ui {
        verticalLayout {
            setSizeFull()
            justifyContentMode = FlexComponent.JustifyContentMode.CENTER
            alignItems = FlexComponent.Alignment.CENTER

            h1("Created by: Austin & Ryan")
            horizontalLayout {
                justifyContentMode = FlexComponent.JustifyContentMode.CENTER
                alignItems = FlexComponent.Alignment.CENTER

                icon(VaadinIcon.INFO_CIRCLE)
                text(
                    " This Application is using Vaadin version ${VaadinCoreShrinkWrap::class.java.getAnnotation(
                        NpmPackage::class.java
                    ).version}"
                )
            }
        }
    }
}
