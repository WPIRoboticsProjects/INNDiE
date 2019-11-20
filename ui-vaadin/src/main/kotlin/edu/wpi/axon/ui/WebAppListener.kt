package edu.wpi.axon.ui

import edu.wpi.axon.dsl.defaultModule
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener
import mu.KotlinLogging
import org.koin.core.context.startKoin

@WebListener
class WebAppListener : ServletContextListener {

    override fun contextInitialized(sce: ServletContextEvent?) {
        LOGGER.info { "Starting web app." }

        startKoin {
            modules(defaultModule())
        }
    }

    override fun contextDestroyed(sce: ServletContextEvent?) {
        LOGGER.info { "Stopping web app." }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
