import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("fabric-loom")
    `maven-publish`
    java
}

group = property("maven_group")!!
version = property("mod_version")!!

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://maven.meteordev.org/releases") }
    maven { url = uri("https://nexus.resourcefulbees.com/repository/maven-public/") }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

    val lwjglVersion = property("minecraft_lwjgl_version") as String

    modImplementation("org.lwjgl:lwjgl:$lwjglVersion")
    include("org.lwjgl:lwjgl:$lwjglVersion")

    modImplementation("org.lwjgl:lwjgl-nanovg:$lwjglVersion")
    include("org.lwjgl:lwjgl-nanovg:$lwjglVersion")

    listOf("windows", "linux", "macos").forEach {
        modImplementation("org.lwjgl:lwjgl:$lwjglVersion:natives-$it")
        include("org.lwjgl:lwjgl:$lwjglVersion:natives-$it")

        modImplementation("org.lwjgl:lwjgl-nanovg:$lwjglVersion:natives-$it")
        include("org.lwjgl:lwjgl-nanovg:$lwjglVersion:natives-$it")
    }

    implementation("meteordevelopment:orbit:0.2.3")
    include("meteordevelopment:orbit:0.2.3")

    implementation("com.github.Stivais:Commodore:1.0.0")
    include("com.github.Stivais:Commodore:1.0.0")
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
