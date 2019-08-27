description = "The core project."

dependencies {
    testImplementation(
        group = "com.natpryce",
        name = "hamkrest",
        version = property("hamkrest.version") as String
    )
    testImplementation(
        group = "io.mockk",
        name = "mockk",
        version = property("mockk.version") as String
    )
}
