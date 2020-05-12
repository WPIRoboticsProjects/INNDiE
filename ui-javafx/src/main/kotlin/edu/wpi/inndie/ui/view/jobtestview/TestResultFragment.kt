package edu.wpi.inndie.ui.view.jobtestview

import edu.wpi.inndie.ui.view.jobresult.LazyResult
import edu.wpi.inndie.ui.view.jobresult.ResultFragment
import java.io.File
import javafx.collections.FXCollections
import javafx.scene.control.SelectionMode
import tornadofx.Fragment
import tornadofx.hbox
import tornadofx.listview
import tornadofx.objectBinding

class TestResultFragment : Fragment() {

    val files = FXCollections.observableArrayList<File>()

    override val root = hbox(10) {
        val resultFragment = find<ResultFragment>()

        listview(files) {
            selectionModel.selectionMode = SelectionMode.SINGLE
            isEditable = false
            resultFragment.data.bind(
                selectionModel.selectedItemProperty().objectBinding {
                    it?.let {
                        LazyResult(it.name, lazy { it })
                    }
                }
            )
        }

        add(resultFragment)
    }
}
