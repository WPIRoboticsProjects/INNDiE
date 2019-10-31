plugins {
    kotlin("kapt")
}

apply {
    from(rootProject.file("gradle/generated-kotlin-sources.gradle"))
}

description = "A DSL for generating scripts."

fun DependencyHandler.arrow(name: String) =
    create(group = "io.arrow-kt", name = name, version = property("arrow.version") as String)

fun DependencyHandler.koin(name: String) =
    create(group = "org.koin", name = name, version = property("koin.version") as String)

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

    api(koin("koin-core"))
    api(project(":dsl-interface"))
    api(project(":tf-data"))

    implementation(project(":util"))
    implementation(project(":tf-data-code"))
    implementation(project(":logging"))
    implementation(project(":tf-layer-loader"))

    implementation(
        group = "com.mitchtalmadge",
        name = "ascii-data",
        version = property("ascii-data.version") as String
    )

    testImplementation(project(":dsl-test-util"))
}
