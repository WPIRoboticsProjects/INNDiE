plugins {
    id("org.gretty")
    id("com.devsoap.vaadin-flow")
}

dependencies {
    implementation(project(":tf-data"))
    implementation(project(":training"))

    implementation("com.github.mvysny.karibudsl:karibu-dsl-v10:0.7.0")

    fun jetty(
        group: String = "org.eclipse.jetty",
        name: String,
        version: String = property("jetty.version") as String
    ) = create(group = group, name = name, version = version)

    implementation(jetty(name = "jetty-server"))
    implementation(jetty(name = "jetty-webapp"))
    implementation(jetty(group = "org.eclipse.jetty.websocket", name = "websocket-server"))

    testImplementation(
        group = "com.github.mvysny.kaributesting",
        name = "karibu-testing-v10",
        version = property("karibu-testing-v10.version") as String
    )
}

gretty {
    // https://akhikhl.github.io/gretty-doc/Gretty-configuration.html
    host = "localhost"
    httpPort = 8080
    contextPath = "axon"
}

node {
    download = true
}

vaadin {
    version = property("vaadin.version") as String
    isProductionMode = false
    isSubmitStatistics = false
    autoconfigure()
}
