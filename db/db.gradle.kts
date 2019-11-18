description = "Classes to interact with a database"

repositories {
    maven("https://dl.bintray.com/kotlin/exposed")
}

dependencies {
    api(project(":db-data"))
    api(project(":tf-data"))

    implementation(project(":util"))

    testImplementation(project(":test-util"))
    testImplementation(project(":db-data-test-util"))

    implementation("org.jetbrains.exposed:exposed:0.17.7")

    implementation(
        group = "com.beust",
        name = "klaxon",
        version = property("klaxon.version") as String
    )

}
