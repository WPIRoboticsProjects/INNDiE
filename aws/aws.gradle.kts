description = "Interfaces with AWS using their Java SDK."

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

    // implementation(platform("software.amazon.awssdk:bom:2.9.9"))
    implementation(group = "software.amazon.awssdk", name = "aws-sdk-java", version = "2.9.9")

    implementation(project(":logging"))
    implementation(project(":util"))
}
