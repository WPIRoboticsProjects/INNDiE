package edu.wpi.axon.ui

import edu.wpi.axon.db.data.Job
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.TableCell

class JobIdTableCell : TableCell<Job, Int>() {

    override fun updateItem(id: Int?, empty: Boolean) {
        super.updateItem(id, empty)
        if (empty || id == null) {
            text = null
            graphic = null
        } else {
            alignment = Pos.CENTER_LEFT
            graphic = Label(id.toString())
        }
    }
}
