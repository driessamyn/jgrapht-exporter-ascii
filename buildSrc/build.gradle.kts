plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.kover.plugin)
    implementation(libs.deployer.plugin)
    // Deployer references KotlinTarget at class-decoration time; align with Gradle's embedded Kotlin.
    implementation(embeddedKotlin("gradle-plugin"))
    implementation("com.diffplug.spotless:com.diffplug.spotless.gradle.plugin:8.2.1")
}
