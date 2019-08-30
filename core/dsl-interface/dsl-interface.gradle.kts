description = "Interfaces and APIs for the script generator DSL."

fun DependencyHandler.arrow(name: String) =
    create(group = "io.arrow-kt", name = name, version = property("arrow.version") as String)

fun DependencyHandler.koin(name: String) =
    create(group = "org.koin", name = name, version = property("koin.version") as String)

dependencies {
    api(arrow("arrow-core-data"))
    api(arrow("arrow-extras-data"))

    api(koin("koin-core"))
}
