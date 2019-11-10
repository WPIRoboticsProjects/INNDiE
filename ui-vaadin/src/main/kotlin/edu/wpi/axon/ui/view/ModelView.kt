package edu.wpi.axon.ui.view

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.addColumnFor
import com.github.mvysny.karibudsl.v10.flexGrow
import com.github.mvysny.karibudsl.v10.grid
import com.github.mvysny.karibudsl.v10.splitLayout
import com.github.mvysny.karibudsl.v10.textArea
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.router.Route
import edu.wpi.axon.ui.AxonLayout
import edu.wpi.axon.ui.view.component.JobsMiniList

@Route(layout = AxonLayout::class)
class ModelView : KComposite() {
    private val models = listOf(
            Model("UNet", "This model is great at detecting horseshoes"),
            Model("MobileNet", "This model is great at detecting mobile homes"),
            Model("resnet", "This model is not that great"),
            Model("mnist", "This model is great at detecting numbers")
    )

    private val root = ui {
        splitLayout {
            addToPrimary(JobsMiniList())
            addToSecondary(VerticalLayout().apply {
                grid(dataProvider = ListDataProvider(models)) {
                    flexGrow = 0.0
                    addColumnFor(Model::name)
                    addColumnFor(Model::description).isSortable = false

                    setSelectionMode(Grid.SelectionMode.SINGLE)
                }
            })
        }
    }
}

data class Model(val name: String, val description: String)
