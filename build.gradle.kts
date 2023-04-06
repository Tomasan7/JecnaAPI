plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
    `java-library`
    `maven-publish`
}

group = "me.tomasan7"
version = "3.2.1"

dependencies {
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

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.dokka")

    dependencies {
        /* Kotlin standard library */
        implementation(kotlin("stdlib"))
        /* Align versions of all Kotlin components. */
        implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    }

    group = rootProject.group
    version = rootProject.version

    tasks {
        compileKotlin {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
        val removeMainFiles = register("removeMainFiles") {
            doLast {
                sourceSets.main.get().allSource.forEach { file ->
                    if (file.name in listOf("Main.kt", "Main.java"))
                    {
                        println("Deleted file: ${file.absolutePath}")
                        file.delete()
                    }
                }
            }
        }
        publishToMavenLocal {
            dependsOn(clean, removeMainFiles)
            mustRunAfter(clean, removeMainFiles)
        }
    }

    java {
        withSourcesJar()
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    kotlin {
        jvmToolchain(17)
    }

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

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                artifact(dokkaJavadocJar)
                artifact(dokkaHtmlJar)

                pom {
                    name.set("JecnaAPI")
                    description.set("A library to access data from the SPSE Jecna web.")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://www.opensource.org/licenses/mit-license.php")
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            id.set("Tomasan7")
                            name.set("Tomáš Hůla")
                            email.set("tomashula06@gmail.com")
                        }
                    }
                }
            }
        }
    }
}
