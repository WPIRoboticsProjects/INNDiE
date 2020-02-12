package edu.wpi.axon.ui.main

import edu.wpi.axon.dsl.defaultBackendModule
import edu.wpi.axon.ui.view.Main
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import tornadofx.App
import tornadofx.DIContainer
import tornadofx.FX
import kotlin.reflect.KClass

class Axon: App(Main::class) {
    init {
        startKoin {
            modules(
                    listOf(
                            defaultBackendModule(),
                            defaultFrontendModule()
                    )
            )
        }

        FX.dicontainer = object : DIContainer, KoinComponent {
            override fun <T : Any> getInstance(type: KClass<T>): T {
                return getKoin().get(clazz = type, qualifier = null, parameters = null)
            }
        }

        importStylesheet("/material.css")
    }

    companion object {
        fun main() {
            tornadofx.launch<Axon>()
        }
    }
}

/*
 * https://github.com/edvin/tornadofx/issues/982
 */
private fun Any.importStylesheet(path: String) {
    val css = javaClass.getResource(path)
    if (css == null) {
        throw IllegalArgumentException("Unable to find $path")
    } else {
        tornadofx.importStylesheet(css.toExternalForm()) // this one works
    }
}