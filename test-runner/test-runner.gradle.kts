description = "Runs inference tests."

dependencies {
    api(project(":plugin"))
    api(project(":tf-data"))

    implementation(project(":dsl"))
    implementation(project(":util"))

    testImplementation(project(":test-util"))
}
