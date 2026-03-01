plugins {
    id("jgrapht-ascii.library-conventions")
    id("jgrapht-ascii.library-publish")
}

dependencies {
    api(libs.jgrapht.core)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
