description = "Interfaces with AWS using their Java SDK."

dependencies {
    // implementation(platform("software.amazon.awssdk:bom:2.9.9"))
    implementation(group = "software.amazon.awssdk", name = "aws-sdk-java", version = "2.9.9")
}
