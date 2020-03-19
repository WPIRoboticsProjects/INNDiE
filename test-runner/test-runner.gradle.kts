description = "Runs inference tests."

dependencies {
    api(project(":plugin"))

    implementation(project(":dsl"))
    implementation(project(":util"))

    testImplementation(project(":test-util"))
}
