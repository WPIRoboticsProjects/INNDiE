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

    // api(
    //     group = "io.kotlintest",
    //     name = "kotlintest-assertions",
    //     version = property("kotlintest.version") as String
    // )
    // api(
    //     group = "io.kotlintest",
    //     name = "kotlintest-assertions-arrow",
    //     version = property("kotlintest.version") as String
    // )

    // TODO: Go back to the old dependencies once 4.x.x is out
    // https://github.com/wpilibsuite/Axon/issues/84
    api(fileTree("$rootDir/libraries") {
        include("*.jar")
        exclude {
            it.file.name.contains("runner")
        }
    })

    // Needed by kotlintest
    api(group = "com.github.wumpz", name = "diffutils", version = "2.2")

    api(
        group = "org.junit.jupiter",
        name = "junit-jupiter",
        version = property("junit-jupiter.version") as String
    )
}
