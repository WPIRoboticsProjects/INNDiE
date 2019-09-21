plugins {
    id("org.gretty")
    id("com.devsoap.vaadin-flow")
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
}

dependencies {
    implementation(platform(vaadin.bom()))
    implementation(vaadin.core())
    implementation(vaadin.lumoTheme())

    testImplementation(
        group = "com.github.mvysny.kaributesting",
        name = "karibu-testing-v10",
        version = property("karibu-testing-v10.version") as String
    )
}
