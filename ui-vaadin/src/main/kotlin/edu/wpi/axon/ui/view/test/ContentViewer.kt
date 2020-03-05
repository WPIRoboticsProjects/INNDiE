package edu.wpi.axon.ui.view.test

import arrow.core.None
import arrow.core.Option
import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.h2
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.HasText
import edu.wpi.axon.ui.component.DynamicContentViewer
import edu.wpi.axon.ui.component.dynamicContentViewer
import java.io.File

class ContentViewer : KComposite() {

    private lateinit var title: HasText
    private lateinit var content: DynamicContentViewer

    var file: Option<File> = None
        set(value) {
            field = value
            value.fold({
                title.text = "No file selected"
                content.clear()
            }, {
                title.text = it.name
                content.setContent(it)
            })
        }

    init {
        ui {
            verticalLayout {
                title = h2("No file selected")
                content = dynamicContentViewer()
                style.set("background-color", "red")
            }
        }
    }
}
