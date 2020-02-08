plugins {
    id("application")
    id("org.openjfx.javafxplugin")
}

description = "UI code using JavaFX"

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

    implementation(
        group = "org.jetbrains.kotlinx",
        name = "kotlinx-coroutines-javafx",
        version = property("kotlin-coroutines.version") as String
    )

    implementation(
        group = "org.jetbrains.exposed",
        name = "exposed",
        version = property("exposed.version") as String
    )

    testImplementation(project(":training-test-util"))
}

javafx {
    version = "13"
    modules = listOf(
        "javafx.base",
        "javafx.controls",
        "javafx.fxml",
        "javafx.graphics",
        "javafx.media",
        "javafx.swing",
        "javafx.web"
    )
}

application {
    mainClassName = "edu.wpi.axon.ui.MainUI"
}
