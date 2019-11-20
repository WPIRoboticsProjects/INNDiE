description = "Data descriptions used in the database."

dependencies {
    api(project(":tf-data"))

    implementation(project(":util"))

    testImplementation(project(":test-util"))
    testImplementation(project(":db-data-test-util"))
}
