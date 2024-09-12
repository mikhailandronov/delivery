repositories {
    mavenCentral()
}

plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

dependencies {
    // testImplementation(libs.junit.jupiter.engine)
    testImplementation(kotlin("test"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(19)
    }
}

application {
    // mainClass = "org.ama.delivery.app.AppKt"
    mainClass = "org.ama.delivery.app.SandboxKt"
}

tasks.test {
    useJUnitPlatform()
}