repositories {
    mavenCentral()
}

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    id("com.autonomousapps.dependency-analysis") version "2.0.1" // to enable buildHealth Gradle task
    id("com.dorongold.task-tree") version "4.0.0" // to enable taskTree Gradle task
}
