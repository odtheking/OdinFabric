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
    maven("https://jitpack.io")
    maven("https://maven.meteordev.org/releases")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://maven.terraformersmc.com/")
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")
    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.1")

    implementation("meteordevelopment:orbit:0.2.3")
    include("meteordevelopment:orbit:0.2.3")

    implementation("com.github.stivais:Commodore:1.0.1")
    include("com.github.stivais:Commodore:1.0.1")

    modCompileOnly("com.terraformersmc:modmenu:${property("modmenu_version")}")

    val lwjglVersion = property("minecraft_lwjgl_version")

    modImplementation("org.lwjgl:lwjgl-nanovg:$lwjglVersion")
    include("org.lwjgl:lwjgl-nanovg:$lwjglVersion")

    listOf("windows", "linux", "macos", "macos-arm64").forEach {
        modImplementation("org.lwjgl:lwjgl-nanovg:$lwjglVersion:natives-$it")
        include("org.lwjgl:lwjgl-nanovg:$lwjglVersion:natives-$it")
    }
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
