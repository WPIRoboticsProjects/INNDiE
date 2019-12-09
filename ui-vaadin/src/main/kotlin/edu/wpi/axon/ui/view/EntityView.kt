package edu.wpi.axon.ui.view

/**
 * A master / detail view for entities of the type `T`. The view
 * has a list of entities (the 'master' part) and a dialog to show a single
 * entity (the 'detail' part).
 *
 * The view can also show notifications, error messages, and confirmation
 * requests.
 *
 * @param <T> the entity type
</T> */
interface EntityView<T> : HasNotifications {
    val entityName: String

    /**
     * Shows an error notification with a given text.
     *
     * @param message a user-friendly error message
     * @param isPersistent if `true` the message requires a user action to disappear (if `false` it disappears automatically after some time)
     */
    fun showError(message: String, isPersistent: Boolean) {
        showNotification(message, isPersistent)
    }

    fun showCreatedNotification() {
        showNotification("$entityName was created")
    }

    fun showUpdatedNotification() {
        showNotification("$entityName was updated")
    }

    fun showDeletedNotification() {
        showNotification("$entityName was deleted")
    }
}
