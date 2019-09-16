description = "Logging tools."

dependencies {
    api(
        group = "io.github.microutils",
        name = "kotlin-logging",
        version = property("kotlin-logging.version") as String
    )

    runtimeOnly(
        group = "org.slf4j",
        name = "slf4j-log4j12",
        version = property("slf4j-log4j12.version") as String
    )

    runtimeOnly(
        group = "log4j",
        name = "apache-log4j-extras",
        version = property("apache-log4j-extras.version") as String
    )
}
