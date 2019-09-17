package edu.wpi.axon.ui

import com.vaadin.flow.server.VaadinServlet
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.servlet.ServletContextHandler


fun main() {
    println("Server starting...")

    val embeddedServer = Server(8080)

    val contextHandler = ServletContextHandler(null, "/", true, false)
    embeddedServer.handler = contextHandler

    val sessions = SessionHandler()
    contextHandler.sessionHandler = sessions

    val servletHolder = ServletHolder(VaadinServlet::class.java)
    contextHandler.addServlet(servletHolder, "/*")

    try {
        embeddedServer.start()
        embeddedServer.join()
    } catch (e: Exception) {
        println("Server error:")
        println(e.stackTrace)
    }

    println("Server stopped")
}