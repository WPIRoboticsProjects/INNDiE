plugins {
    kotlin("kapt")
}

apply {
    from(rootProject.file("gradle/generated-kotlin-sources.gradle"))
}

description = "Loads layers from TF Graphs."

fun DependencyHandler.arrow(name: String) =
    create(group = "io.arrow-kt", name = name, version = property("arrow.version") as String)

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
    api(arrow("arrow-effects-data"))
    api(arrow("arrow-effects-extensions"))
    api(arrow("arrow-effects-io-extensions"))
    kapt(arrow("arrow-meta"))

    api(project(":tf-data"))

    implementation(group = "io.jhdf", name = "jhdf", version = property("jhdf.version") as String)
    implementation(
        group = "com.beust",
        name = "klaxon",
        version = property("klaxon.version") as String
    )
    implementation(project(":util"))
    implementation(project(":logging"))

    testImplementation(project(":test-util"))
}
