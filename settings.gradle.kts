@file:Suppress("UnstableApiUsage")

val spotlessPluginVersion: String by settings
val ktlintPluginVersion: String by settings
val spotbugsPluginVersion: String by settings
val detektPluginVersion: String by settings
val dokkaPluginVersion: String by settings
val testloggerPluginVersion: String by settings
val pitestPluginVersion: String by settings

pluginManagement {
    plugins {
        id("com.diffplug.gradle.spotless") version spotlessPluginVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintPluginVersion
        id("com.github.spotbugs") version spotbugsPluginVersion
        id("io.gitlab.arturbosch.detekt") version detektPluginVersion
        id("org.jetbrains.dokka") version dokkaPluginVersion
        id("com.adarshr.test-logger") version testloggerPluginVersion
        id("info.solidsoft.pitest") version pitestPluginVersion
    }
}

rootProject.name = "axon"

include(":dsl")
include(":dsl-interface")
include(":dsl-test-util")
include(":pattern-match")
include(":task-property-testing")
include(":tasks-yolov3")
include(":test-util")
include(":tf-layer-loader")
include(":tf-data-code")
include(":tf-data")
include(":util")

/**
 * This configures the gradle build so we can use non-standard build file names.
 * Additionally, this project can support sub-projects who's build file is written in Kotlin.
 *
 * @param project The project to configure.
 */
fun configureGradleBuild(project: ProjectDescriptor) {
    val projectBuildFileBaseName = project.name
    val gradleBuild = File(project.projectDir, "$projectBuildFileBaseName.gradle")
    val kotlinBuild = File(project.projectDir, "$projectBuildFileBaseName.gradle.kts")
    assert(!(gradleBuild.exists() && kotlinBuild.exists())) {
        "Project ${project.name} can not have both a ${gradleBuild.name} and a ${kotlinBuild.name} file. " +
                "Rename one so that the other can serve as the base for the project's build"
    }
    project.buildFileName = when {
        gradleBuild.exists() -> gradleBuild.name
        kotlinBuild.exists() -> kotlinBuild.name
        else -> throw AssertionError(
            "Project `${project.name}` must have a either a file " +
                    "containing ${gradleBuild.name} or ${kotlinBuild.name}"
        )
    }

    // Any nested children projects also get configured.
    project.children.forEach { configureGradleBuild(it) }
}

configureGradleBuild(rootProject)
