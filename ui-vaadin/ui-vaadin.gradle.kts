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
    api(project(":db"))

    implementation(project(":aws"))
    implementation(project(":dsl"))
    implementation(project(":tf-data"))
    implementation(project(":tf-layer-loader"))
    implementation(project(":training"))
    implementation(project(":util"))
    implementation(project(":logging"))
    implementation(project(":db-test-util"))
    implementation(project(":example-models"))

    implementation(platform(vaadin.bom()))
    implementation(vaadin.core())
    implementation(vaadin.lumoTheme())

    implementation(
        group = "com.infraleap",
        name = "animate-css",
        version = property("animate-css.version") as String
    )

    implementation(
        group = "com.github.mvysny.karibudsl",
        name = "karibu-dsl-v10",
        version = property("karibu-dsl-v10.version") as String
    )

    implementation(
        group = "org.hibernate",
        name = "hibernate-validator",
        version = property("hibernate-validator.version") as String
    )

    implementation(
        group = "org.jetbrains.exposed",
        name = "exposed",
        version = property("exposed.version") as String
    )

    testImplementation(project(":training-test-util"))

    testImplementation(
        group = "com.github.mvysny.kaributesting",
        name = "karibu-testing-v10",
        version = property("karibu-testing-v10.version") as String
    )
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
