package edu.wpi.axon.ui

import com.github.mvysny.karibudsl.v10.div
import com.github.mvysny.karibudsl.v10.drawer
import com.github.mvysny.karibudsl.v10.drawerToggle
import com.github.mvysny.karibudsl.v10.h3
import com.github.mvysny.karibudsl.v10.navbar
import com.github.mvysny.karibudsl.v10.routerLink
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.page.BodySize
import com.vaadin.flow.component.page.Viewport
import edu.wpi.axon.ui.view.AboutView
import edu.wpi.axon.ui.view.TrainingView

/**
 * The main layout of the application
 */
@BodySize(height = "100vh", width = "100vw")
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
class AxonLayout : AppLayout() {
    init {
        isDrawerOpened = false
        navbar {
            drawerToggle()
            h3("Axon")
        }
        drawer {
            div {
                routerLink(VaadinIcon.ACADEMY_CAP, "Training", TrainingView::class)
            }
            div {
                routerLink(VaadinIcon.INFO, "About", AboutView::class)
            }
        }
    }

    override fun showRouterLayoutContent(content: HasElement) {
        super.showRouterLayoutContent(content)
        content.element.classList.add("main-layout")
        isDrawerOpened = false
    }
}
