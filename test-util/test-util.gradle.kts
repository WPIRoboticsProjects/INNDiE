description = "Utilities for testing the other projects."

fun DependencyHandler.arrow(name: String) =
    create(group = "io.arrow-kt", name = name, version = property("arrow.version") as String)

fun DependencyHandler.koin(name: String) =
    create(group = "org.koin", name = name, version = property("koin.version") as String)

dependencies {
    api(arrow("arrow-core-data"))

    api(koin("koin-test"))

    api(
        group = "com.natpryce",
        name = "hamkrest",
        version = property("hamkrest.version") as String
    )

    api(
        group = "io.mockk",
        name = "mockk",
        version = property("mockk.version") as String
    )

    api(
        group = "io.kotlintest",
        name = "kotlintest-assertions",
        version = property("kotlintest.version") as String
    )
    api(
        group = "io.kotlintest",
        name = "kotlintest-assertions-arrow",
        version = property("kotlintest.version") as String
    )

    api(
        group = "org.junit.jupiter",
        name = "junit-jupiter",
        version = property("junit-jupiter.version") as String
    )
}
