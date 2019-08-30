description = "Interfaces and APIs for the script generator DSL."

fun DependencyHandler.koin(name: String) =
    create(group = "org.koin", name = name, version = property("koin.version") as String)

dependencies {
    api(koin("koin-core"))
}
