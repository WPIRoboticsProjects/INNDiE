package edu.wpi.axon.ui.view.test

import arrow.core.Option
import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.navigateToView
import com.github.mvysny.karibudsl.v10.splitLayout
import com.vaadin.flow.component.splitlayout.SplitLayout
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.data.renderer.TextRenderer
import com.vaadin.flow.router.BeforeEvent
import com.vaadin.flow.router.HasUrlParameter
import com.vaadin.flow.router.OptionalParameter
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import edu.wpi.axon.ui.MainLayout
import edu.wpi.axon.ui.component.DynamicContentViewer
import java.io.File

@Route(layout = MainLayout::class)
@PageTitle("Test")
class TestView : KComposite(), HasUrlParameter<String> {

    private val fileProvider = ListDataProvider(File("/Users/austinshalit/Desktop/test").walk().filter {
        DynamicContentViewer.validExtensions.contains(it.extension)
    }.toList())

    private val contentViewer = ContentViewer()
    private val contentSelector = ContentSelector().apply {
        setWidthFull()
        dataProvider = fileProvider
        this.setRenderer(TextRenderer { it.name })
        addValueChangeListener {
            navigateTo(it.value.name)
        }
    }

    init {
        ui {
            splitLayout {
                setSizeFull()
                orientation = SplitLayout.Orientation.HORIZONTAL

                addToPrimary(contentViewer)
                addToSecondary(contentSelector)

                setSplitterPosition(80.0)
            }
        }
    }

    override fun setParameter(event: BeforeEvent?, @OptionalParameter fileName: String?) {
        fileName?.let { fileProvider.items.find { file -> file.name == it } }?.let {
            contentSelector.value = it
            contentViewer.file = Option.fromNullable(it)
        }
    }

    companion object {
        fun navigateTo(fileName: String? = null) = navigateToView(TestView::class, fileName)
    }
}
