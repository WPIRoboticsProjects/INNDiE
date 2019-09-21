import com.moowork.gradle.node.npm.NpmTask

plugins {
    id("com.moowork.node")
}

node {
    download = true
}

val vaadinDir = projectDir.resolve("vaadin")

tasks {
    val cleanVaadinResources by registering(Delete::class) {
        group = "electron"

        delete(vaadinDir)
    }

    val syncVaadinResources by registering(Sync::class) {
        group = "electron"
        dependsOn(":ui-vaadin:buildProduct")

        from(projectDir.resolve("../ui-vaadin/build/output/ui-vaadin"))
        into(vaadinDir)
    }

    val run_buildProduct by registering(NpmTask::class) {
        group = "electron"
        dependsOn("npmInstall", syncVaadinResources)

        setArgs(listOf("start"))
    }

    val bundle_buildProduct by registering(NpmTask::class) {
        group = "electron"
        dependsOn("npmInstall", syncVaadinResources)

        setArgs(listOf("run-script", "bundle"))
    }

    clean {
        dependsOn(cleanVaadinResources)
    }

    assemble {
        dependsOn(syncVaadinResources)
    }
}
