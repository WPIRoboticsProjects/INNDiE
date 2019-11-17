package edu.wpi.axon.ui.view

import com.github.mvysny.karibudsl.v10.KComposite
import com.vaadin.flow.router.Route
import edu.wpi.axon.ui.AxonLayout

@Route(layout = AxonLayout::class)
class ModelView : KComposite() {
//    private val models = listOf(
//            Model("UNet", "This model is great at detecting horseshoes"),
//            Model("MobileNet", "This model is great at detecting mobile homes"),
//            Model("resnet", "This model is not that great"),
//            Model("mnist", "This model is great at detecting numbers")
//    )
//
//    private val root = ui {
//        splitLayout {
//            addToPrimary(JobsList())
//            addToSecondary(VerticalLayout().apply {
//                grid(dataProvider = ListDataProvider(models)) {
//                    flexGrow = 0.0
//                    addColumnFor(Model::name)
//                    addColumnFor(Model::description).isSortable = false
//
//                    setSelectionMode(Grid.SelectionMode.SINGLE)
//                }
//            })
//        }
//    }
}
