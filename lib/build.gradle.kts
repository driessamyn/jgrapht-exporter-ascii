plugins {
    id("jgrapht-ascii.library-conventions")
}

dependencies {
    api(libs.jgrapht.core)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
