plugins {
    id("application")
    id("org.openjfx.javafxplugin")
}

description = "UI code using JavaFX"

dependencies {
    api(project(":db"))
    api(project(":test-runner"))

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

    implementation(
            group = "no.tornado",
            name = "tornadofx",
            version = "2.0.0-SNAPSHOT"
    )

    implementation(
        group = "com.sirolf2009",
        name = "fxgraph",
        version = "0.0.3"
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
    mainClassName = "edu.wpi.inndie.ui.main.Axon"
//    mainClassName = "edu.wpi.inndie.ui.MainUI"
    // https://github.com/edvin/tornadofx/issues/899#issuecomment-569709223
    applicationDefaultJvmArgs += "--add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED"
    applicationDefaultJvmArgs += "--add-opens=javafx.controls/javafx.scene.control=ALL-UNNAMED"
    applicationDefaultJvmArgs += "--add-opens=javafx.controls/javafx.scene.control.skin=ALL-UNNAMED"
    applicationDefaultJvmArgs += "--add-opens=javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED"
}
