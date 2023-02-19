plugins {
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
    id("org.jetbrains.dokka") version "1.7.20"
    `java-library`
    `maven-publish`
}

group = "me.tomasan7"
version = "2.0.0-alpha5"

repositories {
    mavenCentral()
}

dependencies {
    /* Align versions of all Kotlin components. */
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    /* Use the Kotlin standard library. */
    implementation(kotlin("stdlib"))
    /* Kotlin Coroutines - for asynchronous programming. */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    /* Jsoup - for parsing HTML. */
    implementation("org.jsoup:jsoup:1.15.3")
    /* Ktor - for HTTP client. */
    val ktorVersion = "2.2.2"
    api("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    /* Ktor Serialization (just core, so the user decides the format) */
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.4.1")

    testImplementation(kotlin("test"))
}

tasks {
    test {
        useJUnitPlatform()
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    val removeMainFile = register("removeMainFile") {
        doFirst {
            sourceSets.main.get().kotlin.srcDirs.forEach {
                println(it)
                val mainFile = File(it, "me/tomasan7/jecnaapi/Main.kt")
                if (mainFile.exists())
                    mainFile.delete()
            }
        }
    }
    publishToMavenLocal {
        dependsOn(clean, removeMainFile)
        mustRunAfter(removeMainFile)
    }
}

// https://github.com/Kotlin/dokka/blob/master/examples/gradle/dokka-library-publishing-example/build.gradle.kts

val dokkaJavadocJar by tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

val dokkaHtmlJar by tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("html-doc")
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "jecna-api"

            from(components["java"])
            artifact(dokkaJavadocJar)
            artifact(dokkaHtmlJar)
        }
    }
}