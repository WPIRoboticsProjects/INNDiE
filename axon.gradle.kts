import com.adarshr.gradle.testlogger.theme.ThemeType
import com.github.spotbugs.SpotBugsTask
import info.solidsoft.gradle.pitest.PitestTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.diffplug.gradle.spotless")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.github.spotbugs")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.dokka")
    id("com.adarshr.test-logger")
    id("info.solidsoft.pitest")
    `maven-publish`
    `java-library`
    jacoco
    pmd
    checkstyle
}

val dslProject = project(":dsl")
val dslInterfaceProject = project(":dsl-interface")
val dslTestUtilProject = project(":dsl-test-util")
val loggingProject = project(":logging")
val patternMatchProject = project(":pattern-match")
val tasksYolov3Project = project(":tasks-yolov3")
val testUtilProject = project(":test-util")
val tfDataProject = project(":tf-data")
val tfDataCode = project(":tf-data-code")
val tfLayerLoaderProject = project(":tf-layer-loader")
val uiElectronProject = project(":ui-electron")
val uiVaadinProject = project(":ui-vaadin")
val trainingProject = project(":training")
val utilProject = project(":util")

val kotlinProjects = setOf(
    dslProject,
    dslInterfaceProject,
    dslTestUtilProject,
    loggingProject,
    patternMatchProject,
    tasksYolov3Project,
    testUtilProject,
    tfDataProject,
    tfDataCode,
    tfLayerLoaderProject,
    uiElectronProject,
    uiVaadinProject,
    trainingProject,
    utilProject
)

val javaProjects = setOf<Project>() + kotlinProjects

val publishedProjects = setOf(
    dslProject,
    dslInterfaceProject,
    loggingProject,
    patternMatchProject,
    tasksYolov3Project,
    tfDataProject,
    tfDataCode,
    tfLayerLoaderProject,
    uiElectronProject,
    uiVaadinProject,
    trainingProject,
    utilProject
)

val pitestProjects = setOf(
    dslProject,
    patternMatchProject,
    tasksYolov3Project,
    tfDataProject,
    tfDataCode,
    tfLayerLoaderProject,
    uiVaadinProject,
    trainingProject,
    utilProject
)

// val spotlessLicenseHeaderDelimiter = "(@|package|import)"

buildscript {
    repositories {
        mavenCentral() // Needed for kotlin gradle plugin
        maven("https://plugins.gradle.org/m2/")
        maven("https://oss.sonatype.org/content/repositories/staging/")
    }

    configurations.maybeCreate("pitest")

    dependencies {
        // Gives us the KotlinJvmProjectExtension
        classpath(kotlin("gradle-plugin", property("kotlin.version") as String))
        "pitest"("org.pitest:pitest-junit5-plugin:0.9")
    }
}

allprojects {
    version = property("axon.version") as String
    group = "edu.wpi"

    apply {
        plugin("com.diffplug.gradle.spotless")
        plugin("com.adarshr.test-logger")
    }

    repositories {
        jcenter()
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/staging/")
        maven("https://dl.bintray.com/arrow-kt/arrow-kt/")
        maven("https://dl.bintray.com/jamesmudd/jhdf")
        maven("https://dl.bintray.com/octogonapus/maven-artifacts")
    }

    // Configures the Jacoco tool version to be the same for all projects that have it applied.
    pluginManager.withPlugin("jacoco") {
        // If this project has the plugin applied, configure the tool version.
        jacoco {
            toolVersion = property("jacoco-tool.version") as String
        }
    }

    tasks.withType<Test> {
        testLogging {
            showStandardStreams = true
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    testlogger {
        theme = ThemeType.STANDARD_PARALLEL
        showStandardStreams = true
    }

    spotless {
        /*
         * We use spotless to lint the Gradle Kotlin DSL files that make up the build.
         * These checks are dependencies of the `check` task.
         */
        kotlinGradle {
            ktlint(property("ktlint.version") as String)
            trimTrailingWhitespace()
        }
        // freshmark {
        //     trimTrailingWhitespace()
        //     indentWithSpaces(2)
        //     endWithNewline()
        // }
        format("extraneous") {
            target("src/**/*.fxml")
            trimTrailingWhitespace()
            indentWithSpaces(2)
            endWithNewline()
        }
    }
}

configure(javaProjects) {
    apply {
        plugin("java")
        plugin("jacoco")
        plugin("checkstyle")
        plugin("com.github.spotbugs")
        plugin("pmd")
    }

    dependencies {
        testImplementation(
            group = "org.junit.jupiter",
            name = "junit-jupiter",
            version = property("junit-jupiter.version") as String
        )

        // testImplementation(
        //     group = "io.kotlintest",
        //     name = "kotlintest-runner-junit5",
        //     version = property("kotlintest.version") as String
        // )

        // TODO: Go back to the old dependencies once 4.x.x is out
        // https://github.com/wpilibsuite/Axon/issues/84
        testImplementation(
            files(
                "$rootDir/libraries/kotlintest-runner-junit5-jvm-4.0.2631-SNAPSHOT.jar",
                "$rootDir/libraries/kotlintest-runner-console-jvm-4.0.2631-SNAPSHOT.jar",
                "$rootDir/libraries/kotlintest-runner-jvm-jvm-4.0.2631-SNAPSHOT.jar",
                "$rootDir/libraries/kotlintest-core-jvm-4.0.2631-SNAPSHOT.jar"
            )
        )
        testImplementation(
            group = "org.slf4j",
            name = "slf4j-api",
            version = "1.7.25"
        )

        testRuntime(
            group = "org.junit.platform",
            name = "junit-platform-launcher",
            version = property("junit-platform-launcher.version") as String
        )
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.isIncremental = true
    }

    val test by tasks.getting(Test::class) {
        @Suppress("UnstableApiUsage")
        useJUnitPlatform {
            filter {
                includeTestsMatching("*Test")
                includeTestsMatching("*Tests")
                includeTestsMatching("*Spec")
            }

            /*
            These tests just test performance and should not run in CI.
             */
            excludeTags("performance")

            /*
            These tests are too slow to run in CI.
             */
            excludeTags("slow")

            /*
            These tests need some sort of software that can't be reasonably installed on CI servers.
             */
            excludeTags("needsSpecialSoftware")

            if (project.hasProperty("hasDockerSupport")) {
                // includeTags("needsDockerSupport")
            } else {
                excludeTags("needsDockerSupport")
            }
        }

        jvmArgs = listOf("-Xss512m")
        if (project.hasProperty("jenkinsBuild") || project.hasProperty("headless")) {
            jvmArgs?.addAll(
                listOf(
                    "-Djava.awt.headless=true",
                    "-Dtestfx.robot=glass",
                    "-Dtestfx.headless=true",
                    "-Dprism.order=sw",
                    "-Dprism.text=t2k"
                )
            )
        }

        testLogging {
            events(
                TestLogEvent.FAILED,
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED,
                TestLogEvent.STARTED
            )
            displayGranularity = 0
            showExceptions = true
            showCauses = true
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            showStandardStreams = true
        }

        @Suppress("UnstableApiUsage")
        reports.junitXml.destination = file("${rootProject.buildDir}/test-results/${project.name}")
    }

    tasks.withType<JacocoReport> {
        reports {
            html.isEnabled = true
            xml.isEnabled = true
            csv.isEnabled = false
        }
    }

    spotless {
        java {
            googleJavaFormat()
            removeUnusedImports()
            trimTrailingWhitespace()
            indentWithSpaces(2)
            endWithNewline()
            // @Suppress("INACCESSIBLE_TYPE")
            // licenseHeaderFile(
            //        "${rootProject.rootDir}/config/spotless/bowler-kernel.license",
            //        spotlessLicenseHeaderDelimiter
            // )
        }
    }

    checkstyle {
        toolVersion = property("checkstyle-tool.version") as String
    }

    spotbugs {
        toolVersion = property("spotbugs-tool.version") as String
        excludeFilter = file("${rootProject.rootDir}/config/spotbugs/spotbugs-excludeFilter.xml")
    }

    tasks.withType<SpotBugsTask> {
        @Suppress("UnstableApiUsage")
        reports {
            xml.isEnabled = false
            emacs.isEnabled = false
            html.isEnabled = true
        }
    }

    pmd {
        toolVersion = property("pmd-tool.version") as String
        ruleSets = emptyList() // Needed so PMD only uses our custom ruleset
        ruleSetFiles = files("${rootProject.rootDir}/config/pmd/pmd-ruleset.xml")
    }
}

configure(kotlinProjects) {
    val kotlinVersion = property("kotlin.version") as String

    apply {
        plugin("kotlin")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("io.gitlab.arturbosch.detekt")
        plugin("org.jetbrains.dokka")
    }

    repositories {
        maven("https://dl.bintray.com/kotlin/ktor")
        maven("https://dl.bintray.com/kotlin/kotlinx")
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8", kotlinVersion))
        implementation(kotlin("reflect", kotlinVersion))
        implementation(
            group = "org.jetbrains.kotlinx",
            name = "kotlinx-coroutines-core",
            version = property("kotlin-coroutines.version") as String
        )
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjvm-default=enable", "-progressive")
        }
    }

    spotless {
        kotlin {
            ktlint(property("ktlint.version") as String)
            trimTrailingWhitespace()
            indentWithSpaces(2)
            endWithNewline()
            // @Suppress("INACCESSIBLE_TYPE")
            // licenseHeaderFile(
            //        "${rootProject.rootDir}/config/spotless/bowler-kernel.license",
            //        spotlessLicenseHeaderDelimiter
            // )
        }
    }

    detekt {
        toolVersion = property("detekt-tool.version") as String
        input = files("src/main/kotlin", "src/test/kotlin")
        parallel = true
        config = files("${rootProject.rootDir}/config/detekt/config.yml")
    }
}

val jacocoRootReport by tasks.creating(JacocoReport::class) {
    group = "verification"
    dependsOn(subprojects.flatMap { it.tasks.withType(JacocoReport::class) } - this)

    val allSrcDirs = subprojects.map { it.sourceSets.main.get().allSource.srcDirs }
    additionalSourceDirs.setFrom(allSrcDirs)
    sourceDirectories.setFrom(allSrcDirs)
    classDirectories.setFrom(subprojects.map { it.sourceSets.main.get().output })
    executionData.setFrom(subprojects.filter {
        File("${it.buildDir}/jacoco/test.exec").exists()
    }.flatMap { it.tasks.withType(JacocoReport::class).map { it.executionData } })

    reports {
        html.isEnabled = true
        xml.isEnabled = true
        csv.isEnabled = false
    }
}

configure(pitestProjects) {
    apply {
        plugin("info.solidsoft.pitest")
    }

    pitest {
        testPlugin.set("junit5")
        pitestVersion.set("1.4.10")
        threads.set(4)
        avoidCallsTo.set(setOf("kotlin.jvm.internal", "kotlinx.coroutines"))
        excludedMethods.set(
            setOf(
                "hashCode",
                "equals",
                "checkIndexOverflow",
                "throwIndexOverflow",
                "collectionSizeOrDefault"
            )
        )
        excludedClasses.set(
            setOf(
                "NoSuchElementException",
                "NoWhenBranchMatchedException",
                "IllegalStateException"
            )
        )
        timeoutConstInMillis.set(10000)
        mutators.set(setOf("NEW_DEFAULTS"))
    }
}

tasks.withType<PitestTask> {
    onlyIf { project in pitestProjects }
}

configure(publishedProjects) {
    apply {
        // plugin("com.jfrog.bintray")
        plugin("maven-publish")
        plugin("java-library")
    }

    val projectName = "axon"

    task<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        archiveBaseName.set("$projectName-${this@configure.name.toLowerCase()}")
        from(sourceSets.main.get().allSource)
    }

    val dokkaJar by tasks.creating(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles Kotlin docs with Dokka"
        archiveClassifier.set("javadoc")
        archiveBaseName.set("$projectName-${this@configure.name.toLowerCase()}")
        from(tasks.dokka)
    }

    val publicationName = "publication-$projectName-${name.toLowerCase()}"

    publishing {
        publications {
            create<MavenPublication>(publicationName) {
                artifactId = "$projectName-${this@configure.name.toLowerCase()}"
                from(components["java"])
                artifact(tasks["sourcesJar"])
                artifact(dokkaJar)
            }
        }
    }

    // bintray {
    //    val bintrayApiUser = properties["bintray.api.user"] ?: System.getenv("BINTRAY_USER")
    //    val bintrayApiKey = properties["bintray.api.key"] ?: System.getenv("BINTRAY_API_KEY")
    //    user = bintrayApiUser as String?
    //    key = bintrayApiKey as String?
    //    setPublications(publicationName)
    //    with(pkg) {
    //        repo = "maven-artifacts"
    //        name = projectName
    //        userOrg = "commonwealthrobotics"
    //        publish = true
    //        setLicenses("LGPL-3.0")
    //        vcsUrl = "https://github.com/CommonWealthRobotics/bowler-kernel.git"
    //        githubRepo = "https://github.com/CommonWealthRobotics/bowler-kernel"
    //        kotlin.with(version) {
    //            name = property("axon.version") as String
    //            desc = "The heart of the Bowler stack."
    //        }
    //    }
    // }
}

tasks.dokka {
    dependsOn(tasks.classes)
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}

tasks.wrapper {
    gradleVersion = rootProject.property("gradle-wrapper.version") as String
    distributionType = Wrapper.DistributionType.ALL
}
