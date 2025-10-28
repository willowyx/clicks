plugins {
    kotlin("jvm") version "2.2.20"
    application
    id("com.gradleup.shadow") version "9.2.2"
}

kotlin {
    jvmToolchain(21)
}

group = "dev.willowyx"
version = "0.17.0"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

val lwjglVersion = "3.3.6"
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

    implementation("org.lwjgl:lwjgl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-opengl:$lwjglVersion")

    runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:${lwjglNatives(os)}")
    runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglVersion:${lwjglNatives(os)}")
    runtimeOnly("org.lwjgl:lwjgl-opengl:$lwjglVersion:${lwjglNatives(os)}")

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("Main")
    val osName = System.getProperty("os.name").lowercase()

    // should be inherited by tasks
    if ("mac" in osName) {
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
