plugins {
    kotlin("kapt")
}

apply {
    from(rootProject.file("gradle/generated-kotlin-sources.gradle"))
}

description = "The core project."

fun DependencyHandler.arrow(name: String) =
    create(group = "io.arrow-kt", name = name, version = property("arrow.version") as String)

fun DependencyHandler.koin(name: String) =
    create(group = "org.koin", name = name, version = property("koin.version") as String)

dependencies {
    api(arrow("arrow-core-data"))
    api(arrow("arrow-core-extensions"))
    api(arrow("arrow-syntax"))
    api(arrow("arrow-typeclasses"))
    api(arrow("arrow-extras-data"))
    api(arrow("arrow-extras-extensions"))
    api(arrow("arrow-optics"))
    api(arrow("arrow-generic"))
    api(arrow("arrow-recursion-data"))
    api(arrow("arrow-recursion-extensions"))
    kapt(arrow("arrow-meta"))

    api(
        group = "org.octogonapus",
        name = "kt-guava-core",
        version = property("kt-guava-core.version") as String
    )

    implementation(koin("koin-core"))

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
    testImplementation(koin("koin-test"))
}
