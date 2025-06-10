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

    modImplementation("com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-1.21.5:3.5.4")
    modImplementation("com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-fabric-1.21.5:3.5.6")
    include("com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-1.21.5:3.5.4")
    include("com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-fabric-1.21.5:3.5.6")
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