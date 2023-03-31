plugins {
    val kotlinVersion = "1.8.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("org.jetbrains.dokka") version kotlinVersion
    `java-library`
    `maven-publish`
}

group = "me.tomasan7"
version = "3.1.0"

repositories {
    mavenCentral()
}

dependencies {
    /* Align versions of all Kotlin components. */
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    /* Kotlin standard library */
    implementation(kotlin("stdlib"))
    /* Asynchronous programming */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    /* HTML parsing */
    implementation("org.jsoup:jsoup:1.15.4")
    /* HTTP client */
    val ktorVersion = "2.2.4"
    api("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    /* Ktor Serialization (just core, so the user decides the format) */
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.0")

    testImplementation(kotlin("test"))
}

tasks {
    test {
        useJUnitPlatform()
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    val removeMainFile = register("removeMainFile") {
        doFirst {
            sourceSets.main.get().kotlin.srcDirs.forEach {
                val mainFile = File(it, "me/tomasan7/jecnaapi/Main.kt")
                if (mainFile.exists())
                {
                    println("Deleted file: ${mainFile.absolutePath}")
                    mainFile.delete()
                }
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
