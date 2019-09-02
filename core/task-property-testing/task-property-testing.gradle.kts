description = "Automated testing of properties for all Tasks."

dependencies {
    testImplementation(project(":dsl"))
    testImplementation(project(":dsl-test-util"))
    testImplementation(project(":tasks-yolov3"))
}
