pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("com.diffplug.spotless") version "8.2.1" apply false
}

rootProject.name = "jgrapht-exporter-ascii"
include("lib")
project(":lib").name = "jgrapht-exporter-ascii"
