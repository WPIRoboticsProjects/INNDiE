description = "Facilitates training a model."

dependencies {
    implementation(project(":dsl"))
    implementation(project(":tf-layer-loader"))

    testImplementation(project(":test-util"))
}
