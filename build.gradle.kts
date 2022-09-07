plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.dokka") version "1.7.10"
    `java-library`
    `maven-publish`
}

group = "me.tomasan7"
version = "1.1.1"

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
    implementation("org.jsoup:jsoup:1.15.2")
    /* Ktor - for HTTP client. */
    val ktorVersion = "2.0.3"
    api("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")

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