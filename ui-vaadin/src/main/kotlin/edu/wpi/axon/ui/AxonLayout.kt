package edu.wpi.axon.ui

import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.h3
import com.github.mvysny.karibudsl.v10.navbar
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.github.mvysny.karibudsl.v10.routerLink
import com.github.mvysny.karibudsl.v10.tab
import com.github.mvysny.karibudsl.v10.tabs
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.page.BodySize
import com.vaadin.flow.component.page.Viewport
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.server.VaadinSession
import edu.wpi.axon.ui.model.TrainingModel
import edu.wpi.axon.ui.view.AboutView
import edu.wpi.axon.ui.view.DatasetView
import edu.wpi.axon.ui.view.TrainingView

/**
 * The main layout of the application
 */
@BodySize(height = "100vh", width = "100vw")
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
class AxonLayout : AppLayout() {
    init {
        if (VaadinSession.getCurrent().getAttribute(TrainingModel::class.java) == null) {
            VaadinSession.getCurrent().setAttribute(TrainingModel::class.java, TrainingModel())
        }

        isDrawerOpened = false
        navbar {
            h3("Axon")
            addToNavbar()
            tabs {
                orientation = Tabs.Orientation.HORIZONTAL
                tab {
                    add(routerLink(VaadinIcon.CAMERA, "Dataset", DatasetView::class))
                }
                tab {
                    add(routerLink(VaadinIcon.GLOBE_WIRE, "Model", DatasetView::class))
                }
                tab {
                    add(routerLink(VaadinIcon.AUTOMATION, "Training", TrainingView::class))
                }
                tab {
                    add(routerLink(VaadinIcon.INFO, "About", AboutView::class))
                }
            }
            button("Debug") {
                onLeftClick {
                    Notification.show(VaadinSession.getCurrent().getAttribute(TrainingModel::class.java).toString())
                }
            }
        }
    }
}
