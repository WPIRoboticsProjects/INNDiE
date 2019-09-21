package edu.wpi.axon.ui

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.page.BodySize
import com.vaadin.flow.component.page.Viewport
import com.vaadin.flow.router.Route

/**
 * The main view of the application
 */
@Route("")
@BodySize(height = "100vh", width = "100vw")
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
class AxonView : AppLayout() {
    init {
        isDrawerOpened = false
        navbar {
            drawerToggle()
            h3("Axon")
        }
        drawer {
            div {
                label("Training")
                button("Click me") {
                    Notification.show("Clicked!")
                }
            }
        }
        content {
            trainingView()
        }
    }
}
