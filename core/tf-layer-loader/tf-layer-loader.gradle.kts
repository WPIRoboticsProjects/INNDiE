description = "Loads layers from TF Graphs."

dependencies {
    api(project(":tf-layers"))

    implementation(group = "io.jhdf", name = "jhdf", version = property("jhdf.version") as String)
}
