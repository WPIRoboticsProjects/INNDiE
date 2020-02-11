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
    }

    companion object {
        fun main() {
            tornadofx.launch<Axon>()
        }
    }
}