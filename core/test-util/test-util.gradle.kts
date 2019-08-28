description = "Utilities for testing the other projects."

dependencies {
    api(
        group = "com.natpryce",
        name = "hamkrest",
        version = property("hamkrest.version") as String
    )
}
