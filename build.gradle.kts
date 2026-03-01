plugins {
    base
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.git.semver)
}

semver {
    releasePattern = "\\Abuild: release"
}

repositories {
    mavenCentral()
}

val projectVersion = semver.version

subprojects {
    group = "net.samyn"
    version = projectVersion
}

dependencies {
    kover(project(":jgrapht-exporter-ascii"))
}

tasks.check {
    dependsOn(":koverHtmlReport")
    dependsOn(":koverXmlReport")
}
