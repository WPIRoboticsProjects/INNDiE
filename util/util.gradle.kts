description = "Various utilities."

fun DependencyHandler.arrow(name: String) =
    create(group = "io.arrow-kt", name = name, version = property("arrow.version") as String)

dependencies {
    api(arrow("arrow-core-data"))
    api(arrow("arrow-fx"))

    api(
        group = "org.octogonapus",
        name = "kt-guava-core",
        version = property("kt-guava.version") as String
    )

    api(
        group = "org.apache.commons",
        name = "commons-lang3",
        version = property("commons-lang3.version") as String
    )

    api(project(":logging"))
}
