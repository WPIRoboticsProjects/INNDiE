description = "Facilitates training a model."

dependencies {
    implementation(project(":dsl"))
    implementation(project(":tf-layer-loader"))
    implementation(project(":logging"))
    implementation(project(":util"))

    testImplementation(project(":training-test-util"))
}
