description = "Handles the example models."

fun DependencyHandler.arrow(name: String) =
    create(group = "io.arrow-kt", name = name, version = property("arrow.version") as String)

dependencies {
    api(arrow("arrow-core"))
    api(arrow("arrow-core-data"))
    api(arrow("arrow-optics"))
    api(arrow("arrow-fx"))
    api(arrow("arrow-syntax"))
    api(arrow("arrow-free"))
    api(arrow("arrow-free-data"))
    api(arrow("arrow-recursion"))
    api(arrow("arrow-recursion-data"))

    implementation(project(":util"))
    implementation(project(":logging"))

    implementation(
        group = "org.eclipse.jgit",
        name = "org.eclipse.jgit",
        version = "5.5.1.201910021850-r"
    )

    implementation(
        group = "org.apache.commons",
        name = "commons-io",
        version = property("commons-io.version") as String
    )

    testImplementation(project(":test-util"))
}
