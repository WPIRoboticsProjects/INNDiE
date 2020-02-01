package edu.wpi.axon.ui.view.jobs

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.listbox.ListBox
import edu.wpi.axon.training.ModelDeploymentTarget

class DeploymentTargetForm : KComposite() {

    init {
        ui {
            verticalLayout {
                val targetListBox = ListBox<ModelDeploymentTarget>()
                targetListBox.setItems(
                    ModelDeploymentTarget.Desktop,
                    ModelDeploymentTarget.Coral(0.0)
                )
                targetListBox.value = ModelDeploymentTarget.Desktop
                add(targetListBox)
            }
        }
    }
}
