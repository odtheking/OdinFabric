import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI

plugins {
    kotlin("jvm")
    id("fabric-loom")
    `maven-publish`
    java
}

group = property("maven_group")!!
version = property("mod_version")!!

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
    maven { url = URI("https://maven.meteordev.org/releases") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://nexus.resourcefulbees.com/repository/maven-public/") }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    implementation("meteordevelopment:orbit:0.2.3")
    include("meteordevelopment:orbit:0.2.3")

    implementation("com.github.Stivais:Commodore:1.0.0")
    include("com.github.Stivais:Commodore:1.0.0")

    implementation("org.lwjgl:lwjgl-nanovg:3.3.3")
    runtimeOnly("org.lwjgl:lwjgl:3.3.3:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-nanovg:3.3.3:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl:3.3.3:natives-linux")
    runtimeOnly("org.lwjgl:lwjgl-nanovg:3.3.3:natives-linux")
    runtimeOnly("org.lwjgl:lwjgl:3.3.3:natives-macos")
    runtimeOnly("org.lwjgl:lwjgl-nanovg:3.3.3:natives-macos")
    include("org.lwjgl:lwjgl-nanovg:3.3.3")

    implementation("com.github.stivais:AuroraUI:9b71d51d5f0e19631d9b8dd2efae232207827ea7")
    include("com.github.stivais:AuroraUI:9b71d51d5f0e19631d9b8dd2efae232207827ea7")
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(getProperties())
            expand(mutableMapOf("version" to project.version))
        }
    }

    jar {
        from("LICENSE")
    }

    compileKotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
            freeCompilerArgs.add("-Xlambdas=class")
        }
    }

}

java {
    withSourcesJar()
}