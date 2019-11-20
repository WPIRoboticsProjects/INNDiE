description = "Utilities for testing code that trains models."

dependencies {
    api(project(":test-util"))
    api(project(":dsl"))
    api(project(":tf-layer-loader"))

    implementation(project(":logging"))
}
