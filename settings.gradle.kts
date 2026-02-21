plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "jgrapht-exporter-ascii"
include("lib")
project(":lib").name = "jgrapht-exporter-ascii"
