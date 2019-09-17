package edu.wpi.axon.ui

import com.helger.commons.lang.ClassPathHelper
import com.vaadin.flow.server.VaadinServlet
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.util.resource.ResourceCollection
import org.eclipse.jetty.webapp.Configuration
import org.eclipse.jetty.webapp.WebAppContext
import java.io.File
import java.util.*


object ManualJetty {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val server = Server(8080)

        // Specifies the order in which the configurations are scanned.
        val classlist = Configuration.ClassList.setServerDefault(server)
        classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration", "org.eclipse.jetty.plus.webapp.EnvConfiguration", "org.eclipse.jetty.plus.webapp.PlusConfiguration")
        classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration", "org.eclipse.jetty.annotations.AnnotationConfiguration")

        // Creation of a temporal directory.
        val tempDir = File(System.getProperty("java.io.tmpdir"), "JettyTest")
        if (tempDir.exists()) {
            if (!tempDir.isDirectory) {
                throw RuntimeException("Not a directory: $tempDir")
            }
        } else if (!tempDir.mkdirs()) {
            throw RuntimeException("Could not make: $tempDir")
        }

        val context = WebAppContext()
        context.setInitParameter("productionMode", "false")
        // Context path of the application.
        context.contextPath = ""
        // Exploded war or not.
        context.isExtractWAR = false
        context.tempDirectory = tempDir

        // It pulls the respective config from the VaadinServlet.
        context.addServlet(VaadinServlet::class.java, "/*")

        context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*")

        context.isParentLoaderPriority = true
        server.setHandler(context)

        // This add jars to the jetty classpath in a certain syntax and the pattern makes sure to load all of them.
        val resourceList = ArrayList<Resource>()
        for (entry in ClassPathHelper.getAllClassPathEntries()) {
            val file = File(entry)
            if (entry.endsWith(".jar")) {
                resourceList.add(Resource.newResource("jar:" + file.toURI().toURL() + "!/"))
            } else {
                resourceList.add(Resource.newResource(entry))
            }
        }

        // It adds the web application resources. Styles, client-side components, ...
        resourceList.add(Resource.newResource("./src/main/webapp"))
        // The base resource is where jetty serves its static content from.
        context.baseResource = ResourceCollection()

        server.start()
        server.join()
    }
}