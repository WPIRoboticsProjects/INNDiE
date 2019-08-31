description = "Utilities for testing the other projects."

fun DependencyHandler.arrow(name: String) =
    create(group = "io.arrow-kt", name = name, version = property("arrow.version") as String)

dependencies {
    api(arrow("arrow-core-data"))
    api(arrow("arrow-extras-data"))

    api(
        group = "com.natpryce",
        name = "hamkrest",
        version = property("hamkrest.version") as String
    )

    api(
        group = "io.kotlintest",
        name = "kotlintest-assertions-arrow",
        version = property("kotlintest.version") as String
    )
}
