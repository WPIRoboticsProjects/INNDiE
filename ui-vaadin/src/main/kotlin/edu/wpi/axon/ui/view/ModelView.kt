package edu.wpi.axon.ui.view

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.VaadinDsl
import com.github.mvysny.karibudsl.v10.div
import com.github.mvysny.karibudsl.v10.flexGrow
import com.github.mvysny.karibudsl.v10.flexLayout
import com.github.mvysny.karibudsl.v10.horizontalAlignSelf
import com.github.mvysny.karibudsl.v10.horizontalLayout
import com.github.mvysny.karibudsl.v10.init
import com.github.mvysny.karibudsl.v10.text
import com.github.mvysny.karibudsl.v10.textAlign
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.listbox.ListBox
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.function.SerializableFunction
import com.vaadin.flow.router.Route
import edu.wpi.axon.ui.AxonLayout

@Route(layout = AxonLayout::class)
class ModelView : KComposite() {
    private val models = listOf(
            Model("UNet", "Not Started"),
            Model("MobileNet", "Not Started"),
            Model("resnet", "Not Started"),
            Model("mnist", "Not Started")
    )

    private val root = ui {
        verticalLayout {
            listBox<Model>(ListDataProvider(models), SerializableFunction {
                horizontalLayout {
                    div {
                        text(it.name)
                    }
                    div {
                        text(it.description)
                    }
                    justifyContentMode = FlexComponent.JustifyContentMode.START
                }
            })

            alignItems = FlexComponent.Alignment.STRETCH
        }
    }
}

data class Model(val name: String, val description: String)

fun <T : Any?> (@VaadinDsl HasComponents).listBox(dataProvider: DataProvider<T, *>? = null, itemRenderer: SerializableFunction<T, Component>) =
        init(ListBox<T>()) {
            if (dataProvider != null) this.dataProvider = dataProvider
            setRenderer(ComponentRenderer(itemRenderer))
        }