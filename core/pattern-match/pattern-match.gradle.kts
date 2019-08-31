description = "Pattern matching utilities."

fun DependencyHandler.arrow(name: String) =
    create(group = "io.arrow-kt", name = name, version = property("arrow.version") as String)

dependencies {
    api(arrow("arrow-core-data"))
    api(arrow("arrow-extras-data"))
}
