description = "Definitions for TF layers."

fun DependencyHandler.arrow(name: String) =
    create(group = "io.arrow-kt", name = name, version = property("arrow.version") as String)

dependencies {
    api(arrow("arrow-core-data"))
    api(
        group = "org.octogonapus",
        name = "kt-guava-core",
        version = property("kt-guava.version") as String
    )

    testImplementation(project(":test-util"))
}
