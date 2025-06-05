rootProject.name = "Odin"
pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven { url = uri("https://jitpack.io") }

        mavenCentral()
        gradlePluginPortal()
    }

    val loom_version: String by settings
    val fabric_kotlin_version: String by settings
    plugins {
        id("fabric-loom") version loom_version
        id("org.jetbrains.kotlin.jvm") version
                fabric_kotlin_version
                    .split("+kotlin.")[1] // Grabs the sentence after `+kotlin.`
                    .split("+")[0] // Ensures sentences like `+build.1` are ignored
    }
}