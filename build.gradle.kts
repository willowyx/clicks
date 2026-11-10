plugins {
    kotlin("jvm") version "2.2.20"
    application
    id("com.gradleup.shadow") version "9.3.2"
}

kotlin {
    jvmToolchain(21)
}

group = "dev.willowyx"
version = "0.26.1"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

val lwjglVersion = "3.4.1"
val jacksonVersion = "2.22.4"
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

if (os.isMacOsX) {
    val jdk21 = javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    val macAppDirectory = layout.buildDirectory.dir("macos")
    val mainJar = tasks.named<Jar>("jar")
    val macBundleVersion = project.version.toString().removePrefix("0.")

    tasks.register<Exec>("packageMac") {
        group = "distribution"
        description = "package clicks as a native Mac app"
        dependsOn(tasks.named("installDist"))

        doFirst {
            val destination = macAppDirectory.get().asFile
            delete(destination.resolve("clicks.app"))
            commandLine(
                jdk21.get().metadata.installationPath.file("bin/jpackage").asFile.absolutePath,
                "--type", "app-image",
                "--name", "clicks",
                "--app-version", macBundleVersion,
                "--vendor", "willowyx",
                "--mac-package-identifier", "dev.willowyx.clicks",
                "--input", layout.buildDirectory.dir("install/${project.name}/lib").get().asFile.absolutePath,
                "--main-jar", mainJar.get().archiveFileName.get(),
                "--main-class", application.mainClass.get(),
                "--java-options", "-XstartOnFirstThread",
                "--java-options", "--enable-native-access=ALL-UNNAMED",
                "--dest", destination.absolutePath,
            )
        }
    }
}

tasks.register("generateVersionProperties") {
    description = "generate version properties"
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
