description = "Classes to interact with a database"

dependencies {
    api(project(":tf-data"))
    api(project(":training"))
    api(project(":plugin"))

    implementation(project(":util"))

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

    testImplementation(project(":test-util"))
    testImplementation(project(":db-test-util"))
}
