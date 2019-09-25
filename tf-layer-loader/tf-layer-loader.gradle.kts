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
    api(arrow("arrow-core"))
    api(arrow("arrow-core-data"))
    api(arrow("arrow-optics"))
    api(arrow("arrow-fx"))
    api(arrow("arrow-syntax"))
    api(arrow("arrow-free"))
    api(arrow("arrow-free-data"))
    api(arrow("arrow-recursion"))
    api(arrow("arrow-recursion-data"))
    kapt(arrow("arrow-meta"))
    kapt(arrow("arrow-generic"))

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
