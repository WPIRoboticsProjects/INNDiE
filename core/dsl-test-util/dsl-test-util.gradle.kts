description = "Utilities for testing code that uses the DSL."

fun DependencyHandler.koin(name: String) =
    create(group = "org.koin", name = name, version = property("koin.version") as String)

dependencies {
    api(project(":dsl-interface"))

    api(project(":test-util"))
    api(
        group = "io.mockk",
        name = "mockk",
        version = property("mockk.version") as String
    )
    api(koin("koin-test"))
}
