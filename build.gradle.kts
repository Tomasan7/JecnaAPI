plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.plugin.serialization)
    alias(libs.plugins.dokka)
    `java-library`
    `maven-publish`
}

group = "me.tomasan7"
version = "3.3.0"

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.jsoup)
    api(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)

    // Debugging only
    //implementation("io.ktor:ktor-client-logging-jvm:2.2.4")

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
        val checkMainDoesntExist = register("checkMainDoesntExist") {
            doLast {
                sourceSets.main.get().allSource.forEach { file ->
                    if (file.name in listOf("Main.kt", "Main.java"))
                        throw IllegalStateException("Main file found: $file")
                }
            }
        }
        publishToMavenLocal {
            dependsOn(clean, checkMainDoesntExist)
            mustRunAfter(clean, checkMainDoesntExist)
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
