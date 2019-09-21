package edu.wpi.axon.ui

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.page.BodySize
import com.vaadin.flow.component.page.Viewport
import com.vaadin.flow.router.Route

/**
 * The main view of the application
 */
@Route("")
@BodySize(height = "100vh", width = "100vw")
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
class UiView : VerticalLayout() {
    init {
        val hello = Label("Hello Kotlin app!")
        add(hello)

        val button = Button("Click me") {
            Notification.show("Clicked!")
        }
        add(button)
    }
}
