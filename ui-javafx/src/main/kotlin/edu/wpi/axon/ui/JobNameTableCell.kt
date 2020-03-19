package edu.wpi.axon.ui

import edu.wpi.axon.db.data.Job
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.TableCell

class JobNameTableCell : TableCell<Job, String>() {

    override fun updateItem(name: String?, empty: Boolean) {
        super.updateItem(name, empty)
        if (empty || name == null) {
            text = null
            graphic = null
        } else {
            alignment = Pos.CENTER_LEFT
            graphic = Label(name)
        }
    }
}
