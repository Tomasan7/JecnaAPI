plugins {
    kotlin("jvm") version "1.6.21"
    id("org.jetbrains.dokka") version "1.6.21"
    `java-library`
    `maven-publish`
}

group = "me.tomasan7"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    /* Align versions of all Kotlin components. */
    api(platform("org.jetbrains.kotlin:kotlin-bom"))
    /* Use the Kotlin JDK 8 standard library. */
    api(kotlin("stdlib-jdk8"))
    /* Kotlin Coroutines - for asynchronous programming. */
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
    /* Jsoup - for parsing HTML. */
    api("org.jsoup:jsoup:1.15.1")
    /* Ktor - for HTTP client. */
    val ktorVersion = "2.0.2"
    api("io.ktor:ktor-client-core:$ktorVersion")
    api("io.ktor:ktor-client-cio:$ktorVersion")

    testImplementation(kotlin("test"))
    /* Use the Kotlin test library. */
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    /* Use the Kotlin JUnit integration. */
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
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