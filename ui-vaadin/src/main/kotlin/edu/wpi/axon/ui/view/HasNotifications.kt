package edu.wpi.axon.ui.view

import com.vaadin.flow.component.Text
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.notification.Notification

/**
 * Interface for views showing notifications to users
 */
interface HasNotifications {
    fun showNotification(message: String, persistent: Boolean = false) {
        if (persistent) {
            val close = Button("Close")
            val notification = Notification(Text(message), close)
            notification.position = Notification.Position.BOTTOM_START
            notification.duration = 0
            close.addClickListener {
                notification.close()
            }
            notification.open()
        } else {
            Notification.show(message, 3000, Notification.Position.BOTTOM_STRETCH)
        }
    }
}
