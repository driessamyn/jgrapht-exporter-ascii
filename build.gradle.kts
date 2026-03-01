plugins {
    base
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.git.semver)
}

semver {
    releasePattern = "\\Abuild: release"
    releaseCommitTextFormat = "build: release %s\n\n%s"
    releaseTagNameFormat = "v%s"
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
