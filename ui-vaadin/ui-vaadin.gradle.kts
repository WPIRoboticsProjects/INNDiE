plugins {
    id("org.gretty")
    id("com.devsoap.vaadin-flow")
}

gretty {
    // https://akhikhl.github.io/gretty-doc/Gretty-configuration.html
    host = "0.0.0.0"
    httpPort = 8080
    contextPath = ""
}

node {
    download = true
}

vaadin {
    version = property("vaadin.version") as String
    isProductionMode = false
    isSubmitStatistics = false
}

repositories {
    vaadin.addons()
}

dependencies {
    implementation("org.jetbrains.exposed:exposed:0.17.7") // temp

    api(project(":db-data"))

    implementation(project(":aws"))
    implementation(project(":db"))
    implementation(project(":dsl"))
    implementation(project(":tf-data"))
    implementation(project(":tf-layer-loader"))
    implementation(project(":training"))
    implementation(project(":util"))
    implementation(project(":logging"))
    implementation(project(":db-data-test-util"))
    implementation(project(":example-models"))

    implementation(platform(vaadin.bom()))
    implementation(vaadin.core())
    implementation(vaadin.lumoTheme())

    implementation(group = "com.infraleap", name = "animate-css", version = "1.0.1")

    implementation("com.github.mvysny.karibudsl:karibu-dsl-v10:0.7.0")

    implementation(group = "org.hibernate", name = "hibernate-validator", version = "5.4.1.Final")

    testImplementation(
        group = "com.github.mvysny.kaributesting",
        name = "karibu-testing-v10",
        version = property("karibu-testing-v10.version") as String
    )
    testImplementation(project(":training-test-util"))
}

tasks {
    val integrationTest by registering {
        group = "vaadin"
        description = "Will attempt to bring up and stop the vaadin application"
    }
    check {
        dependsOn(integrationTest)
    }
}
