description = "Classes to interact with a database"

dependencies {
    api(project(":db-data"))
    api(project(":tf-data"))

    implementation(project(":util"))

    testImplementation(project(":test-util"))
    testImplementation(project(":db-data-test-util"))

    implementation(
        group = "org.jetbrains.exposed",
        name = "exposed",
        version = property("exposed.version") as String
    )

    implementation(
        group = "com.beust",
        name = "klaxon",
        version = property("klaxon.version") as String
    )
}
