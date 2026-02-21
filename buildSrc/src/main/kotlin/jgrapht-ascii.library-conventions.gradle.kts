import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    `java-library`
    id("org.jetbrains.kotlinx.kover")
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(11)
}

kover {
    reports {
        verify {
            rule {
                bound {
                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                    coverageUnits = CoverageUnit.INSTRUCTION
                    minValue = 90
                }
            }
        }
    }
}

tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
            ),
        )
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    testLogging {
        showStandardStreams = true
    }

    systemProperties(
        "junit.jupiter.execution.parallel.enabled" to "true",
        "junit.jupiter.execution.parallel.mode.default" to "same_thread",
        "junit.jupiter.execution.parallel.mode.classes.default" to "concurrent",
    )
}

tasks.check {
    dependsOn(tasks.koverVerify)
}

tasks.test {
    finalizedBy(tasks.koverXmlReport, tasks.koverHtmlReport)
}
