plugins {
    application
    //id("org.gretty") version "2.3.1"
    id("com.devsoap.vaadin-flow") version "1.2"
}

dependencies {
    fun jetty(group: String = "org.eclipse.jetty", name: String, version: String = "9.4.20.v20190813") =
            create(group = group, name = name, version = version)
    implementation(jetty(name = "jetty-server"))
    implementation(jetty(name = "jetty-webapp"))
    implementation(jetty(group = "org.eclipse.jetty.websocket", name = "websocket-server"))
}

application {
    applicationName = "Axon"
    mainClassName = "edu.wpi.axon.ui.LauncherKt"
}

node {
    download = true
    nodeModulesDir = file("src/main/electron")
}

vaadin {
    version = "14.0.4"
    isProductionMode = false
    isSubmitStatistics = false

    autoconfigure()
}
