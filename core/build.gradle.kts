repositories {
    mavenCentral()
}

plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.arrow.core)
    implementation(libs.arrow.fx.coroutines)

    // testImplementation(libs.junit.jupiter.engine)
    testImplementation(kotlin("test"))
    testImplementation (libs.kotest.runner.junit5)
    testImplementation (libs.kotest.assertions.core)
    testImplementation (libs.kotest.assertions.arrow)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(19)
    }
}

tasks.test {
    useJUnitPlatform()
}