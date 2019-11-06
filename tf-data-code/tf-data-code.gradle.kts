description = "Python implementations for TF layers."

fun DependencyHandler.arrow(name: String) =
    create(group = "io.arrow-kt", name = name, version = property("arrow.version") as String)

fun DependencyHandler.koin(name: String) =
    create(group = "org.koin", name = name, version = property("koin.version") as String)

dependencies {
    api(project(":tf-data"))
    api(koin("koin-core"))
    api(arrow("arrow-core"))

    testImplementation(project(":test-util"))
}
