package edu.wpi.axon.ui.component

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.VaadinDsl
import com.github.mvysny.karibudsl.v10.div
import com.github.mvysny.karibudsl.v10.init
import com.vaadin.flow.component.HasComponents
import java.io.File

class DynamicContentViewer : KComposite() {
    companion object {
        val imageExtensions = setOf("png", "jpg", "jpeg")
        val videoExtensions = setOf("mp4")

        val validExtensions = imageExtensions + videoExtensions
    }

    private lateinit var image: DynamicImage
    private lateinit var video: DynamicVideo

    init {
        ui {
            div {
                width = "100%"
                height = "90%"
                image = dynamicImage {
                    setSizeFull()
                    style["object-fit"] = "contain"
                    isVisible = false
                }
                video = dynamicVideo {
                    setSizeFull()
                    style["object-fit"] = "contain"
                    isVisible = false
                }
                style.set("background-color", "lightblue")
            }
        }
    }

    fun setContent(source: File) {
        clear()

        if (imageExtensions.contains(source.extension)) {
            image.setFile(source)
            image.isVisible = true
        } else if (videoExtensions.contains(source.extension)) {
            video.setFile(source)
            video.isVisible = true
        }
    }

    fun clear() {
        image.clear()
        image.isVisible = false
        video.clear()
        video.isVisible = false
    }
}

@VaadinDsl
fun (@VaadinDsl HasComponents).dynamicContentViewer(block: (@VaadinDsl DynamicContentViewer).() -> Unit = {}) =
        init(DynamicContentViewer(), block)
