plugins {
    base
    id("org.jetbrains.kotlinx.kover")
}

repositories {
    mavenCentral()
}

dependencies {
    kover(project(":jgrapht-exporter-ascii"))
}

tasks.check {
    dependsOn(":koverHtmlReport")
    dependsOn(":koverXmlReport")
}
