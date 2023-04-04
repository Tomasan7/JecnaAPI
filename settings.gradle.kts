rootProject.name = "jecnaapi"
include("jecnaapi-java")

pluginManagement {
    val kotlinVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("org.jetbrains.dokka") version kotlinVersion
        `java-library`
        `maven-publish`
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
