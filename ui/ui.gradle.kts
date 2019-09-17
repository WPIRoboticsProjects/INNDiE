plugins {
    application
    id("org.openjfx.javafxplugin") version "0.0.8"
}

dependencies {
    implementation(project(":tf-data"))
    implementation(project(":training"))

    implementation(group = "no.tornado", name = "tornadofx", version = "1.7.19")
}

application {
    mainClassName = "edu.wpi.axon.ui.Axon"
}

javafx {
    modules("javafx.controls", "javafx.fxml")
}
