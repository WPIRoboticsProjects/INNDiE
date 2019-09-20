import com.moowork.gradle.node.npm.NpmTask

plugins {
    id("com.moowork.node") version "1.3.1"
}

node {
    download = true
}

tasks {
    val syncVaadinResources by registering(Sync::class) {
        group = "electron"
        dependsOn(":ui-vaadin:buildProduct")

        from(projectDir.resolve("../ui-vaadin/build/output/ui-vaadin"))
        into(projectDir.resolve("vaadin"))
    }
    val run by registering(NpmTask::class) {
        group = "electron"
        dependsOn("npmInstall", syncVaadinResources)

        setArgs(listOf("start"))
    }
}
