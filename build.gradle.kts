plugins {
    kotlin("jvm") version "2.2.20"
    application
    id("com.gradleup.shadow") version "9.2.2"
}

kotlin {
    jvmToolchain(21)
}

group = "dev.willowyx"
version = "0.23.2"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

val lwjglVersion = "3.3.6"
val jacksonVersion = "2.17.2"
val os = org.gradle.internal.os.OperatingSystem.current()

fun lwjglNatives(os: org.gradle.internal.os.OperatingSystem): String = when {
    os.isWindows -> "natives-windows"
    os.isLinux -> "natives-linux"
    os.isMacOsX -> if (System.getProperty("os.arch") == "aarch64") {
        "natives-macos-arm64"
    } else {
        "natives-macos"
    }
    else -> error("Unsupported OS")
}

dependencies {
    implementation("com.github.SpaiR.imgui-java:imgui-java-app:v1.90.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${jacksonVersion}")

    implementation("org.lwjgl:lwjgl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-opengl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-tinyfd:$lwjglVersion")

    runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:${lwjglNatives(os)}")
    runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglVersion:${lwjglNatives(os)}")
    runtimeOnly("org.lwjgl:lwjgl-opengl:$lwjglVersion:${lwjglNatives(os)}")
    runtimeOnly("org.lwjgl:lwjgl-tinyfd:$lwjglVersion:${lwjglNatives(os)}")
}

application {
    mainClass.set("Main")
    if (org.gradle.internal.os.OperatingSystem.current().isMacOsX) {
        applicationDefaultJvmArgs = listOf("-XstartOnFirstThread", "--enable-native-access=ALL-UNNAMED")
    }
}

tasks.register("generateVersionProperties") {
    val outputDir = layout.buildDirectory.dir("generated/resources/version")
    outputs.dir(outputDir)

    doLast {
        val versionFile = outputDir.get().file("version.properties").asFile
        versionFile.parentFile.mkdirs()
        versionFile.writeText("version=${project.version}\n")
    }
}

sourceSets.main {
    resources.srcDir(tasks.named("generateVersionProperties"))
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
}
