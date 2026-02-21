plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.kover.plugin)
    implementation("com.diffplug.spotless:com.diffplug.spotless.gradle.plugin:8.2.1")
}
