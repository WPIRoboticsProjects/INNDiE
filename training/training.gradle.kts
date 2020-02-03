description = "Facilitates training a model."

dependencies {
    api(project(":plugin"))

    implementation(project(":dsl"))
    implementation(project(":tf-layer-loader"))
    implementation(project(":logging"))
    implementation(project(":util"))

    testImplementation(project(":training-test-util"))
}
